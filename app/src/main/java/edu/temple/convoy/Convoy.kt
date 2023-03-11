package edu.temple.convoy

class Convoy () {
    private val participants = ArrayList<Participant>()
    val size: Int
        get() = participants.size

    fun updateConvoy (convoy: Convoy) {
        // Remove missing participants
        participants.forEach {
            if (!convoy.participants.contains(it)) {
                participants.remove(it)
                it.removeMarker()
            }
        }

        // Add new participants and update old participants
        convoy.participants.forEach {
            if (!participants.contains(it)) {
                participants.add(it)
            } else {
                participants[participants.indexOf(it)].latLng = it.latLng
            }
        }
    }

    fun addParticipant(participant: Participant) {
        participants.add(participant)
    }

    fun getParticipant (index: Int): Participant {
        return participants[index]
    }

}