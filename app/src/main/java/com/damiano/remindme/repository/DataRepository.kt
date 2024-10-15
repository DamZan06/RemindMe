package com.damiano.remindme.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val userEventsRef: DatabaseReference = database.getReference("users").child(currentUserId).child("events")
    private val userSegmentsRef: DatabaseReference = database.getReference("users").child(currentUserId).child("segments")

    fun generateUniqueId(): String {
        return userEventsRef.push().key ?: ""
    }

    fun saveEvent(event: Event) {
        val eventId = userEventsRef.push().key ?: return
        userEventsRef.child(eventId).setValue(event)
    }

    fun deleteEvent(eventId: String, callback: (Boolean) -> Unit) {
        userSegmentsRef.orderByChild("eventId").equalTo(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var success = true
                val tasks = mutableListOf<Task<Void>>()

                for (segmentSnapshot in snapshot.children) {
                    tasks.add(segmentSnapshot.ref.removeValue().addOnFailureListener {
                        success = false
                    })
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    userEventsRef.orderByChild("eventId").equalTo(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var success = true
                            val tasks = mutableListOf<Task<Void>>()

                            for (eventSnapshot in snapshot.children) {
                                tasks.add(eventSnapshot.ref.removeValue().addOnFailureListener {
                                    success = false
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(false)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    fun saveEventSegment(segment: EventSegment) {
        val segmentId = userSegmentsRef.push().key ?: return
        userSegmentsRef.child(segmentId).setValue(segment)
    }

    fun getEventsForDate(date: String, callback: (List<Event>) -> Unit) {
        userEventsRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (childSnapshot in snapshot.children) {
                    val event = childSnapshot.getValue(Event::class.java)
                    event?.let { events.add(it) }
                }
                callback(events)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun getEvents(callback: (List<Event>) -> Unit) {
        userEventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (data in snapshot.children) {
                    val event = data.getValue(Event::class.java)
                    if (event != null) {
                        events.add(event)
                    }
                }
                callback(events)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getEventSegments(callback: (List<EventSegment>) -> Unit) {
        userSegmentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val segments = mutableListOf<EventSegment>()
                for (data in snapshot.children) {
                    val segment = data.getValue(EventSegment::class.java)
                    if (segment != null) {
                        segments.add(segment)
                    }
                }
                callback(segments)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getAllEvents(callback: (List<Event>) -> Unit) {
        userEventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (data in snapshot.children) {
                    val event = data.getValue(Event::class.java)
                    if (event != null) {
                        events.add(event)
                    }
                }
                callback(events)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

}
