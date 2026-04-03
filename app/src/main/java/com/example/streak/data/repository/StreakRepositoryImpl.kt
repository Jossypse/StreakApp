package com.example.streak.data.repository

import com.example.streak.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StreakRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val users = firestore.collection("users")

    suspend fun upsertCurrentUser(user: User) {
        val data = mapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "photoUrl" to user.photoUrl
        )
        users.document(user.uid).set(data).await()
    }

    suspend fun searchUsers(query: String, excludeUid: String): List<User> {
        if (query.isBlank()) return emptyList()

        // Simple prefix email search using orderBy + startAt/endAt
        // This finds users whose email starts with the query text.
        val snapshot = users
            .orderBy("email")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(10)
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { doc ->
                val uid = doc.getString("uid") ?: return@mapNotNull null
                if (uid == excludeUid) return@mapNotNull null
                User(
                    uid = uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    photoUrl = doc.getString("photoUrl")
                )
            }
    }

    suspend fun addStreakPartner(currentUid: String, partner: User) {
        val currentPartners = users.document(currentUid).collection("partners")
        val partnerPartners = users.document(partner.uid).collection("partners")

        val partnerData = mapOf(
            "uid" to partner.uid,
            "name" to partner.name,
            "email" to partner.email,
            "photoUrl" to partner.photoUrl
        )

        val currentUserDoc = users.document(currentUid).get().await()
        val currentUser = User(
            uid = currentUid,
            name = currentUserDoc.getString("name") ?: "",
            email = currentUserDoc.getString("email") ?: "",
            photoUrl = currentUserDoc.getString("photoUrl")
        )

        val currentUserData = mapOf(
            "uid" to currentUser.uid,
            "name" to currentUser.name,
            "email" to currentUser.email,
            "photoUrl" to currentUser.photoUrl
        )

        // Write symmetric partner documents
        currentPartners.document(partner.uid).set(partnerData).await()
        partnerPartners.document(currentUid).set(currentUserData).await()
    }

    suspend fun getStreakPartners(uid: String): List<User> {
        val snapshot = users
            .document(uid)
            .collection("partners")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            User(
                uid = doc.getString("uid") ?: "",
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                photoUrl = doc.getString("photoUrl")
            )
        }
    }

    suspend fun sendStreakPartnerRequest(requester: User, target: User) {
        val requestData = mapOf(
            "requesterUid" to requester.uid,
            "name" to requester.name,
            "email" to requester.email,
            "photoUrl" to requester.photoUrl,
            "status" to "pending"
        )

        users
            .document(target.uid)
            .collection("partnerRequests")
            .document(requester.uid)
            .set(requestData)
            .await()
    }

    suspend fun getIncomingPartnerRequests(uid: String): List<User> {
        val snapshot = users
            .document(uid)
            .collection("partnerRequests")
            .whereEqualTo("status", "pending")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            User(
                uid = doc.getString("requesterUid") ?: doc.id,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                photoUrl = doc.getString("photoUrl")
            )
        }
    }

    suspend fun acceptPartnerRequest(currentUser: User, requesterUid: String) {
        val requestRef = users
            .document(currentUser.uid)
            .collection("partnerRequests")
            .document(requesterUid)

        val requestDoc = requestRef.get().await()
        if (!requestDoc.exists()) return

        val requester = User(
            uid = requestDoc.getString("requesterUid") ?: requesterUid,
            name = requestDoc.getString("name") ?: "",
            email = requestDoc.getString("email") ?: "",
            photoUrl = requestDoc.getString("photoUrl")
        )

        // Move the relationship into "accepted partners"
        addStreakPartner(currentUser.uid, requester)

        // Remove request
        requestRef.delete().await()
    }
}

