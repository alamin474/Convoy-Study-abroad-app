package com.example.data.security

import com.example.data.model.Application
import com.example.data.model.StudentDocument
import com.example.data.model.User
import com.example.data.model.UserRole

/**
 * Manages role-based access control (RBAC), data privacy boundaries,
 * and security rules specifications for Convoy's backend.
 */
object ConvoySecurityManager {

    /**
     * Active current user session context in the mobile application.
     */
    var currentUser: User = User(
        userId = "student_101",
        name = "Alex Mercer",
        email = "alex.mercer@student.org",
        role = UserRole.STUDENT
    )
        private set

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    /**
     * Checks if current user is permitted to write or manage administrative content
     * (Universities, Scholarships, Countries, Programs, Announcements).
     */
    fun canManageAdminContent(): Boolean {
        return currentUser.role == UserRole.ADMIN
    }

    /**
     * Enforces privacy checks for student profiles.
     * Students may ONLY read and write their own profile. Profile details are NEVER public.
     */
    fun canAccessProfile(profileUserId: String): Boolean {
        if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COUNSELOR) {
            return true
        }
        return profileUserId == currentUser.userId
    }

    /**
     * Enforces privacy checks for student applications.
     * Students may ONLY read and write applications tied to their own userId.
     */
    fun canAccessApplication(application: Application): Boolean {
        if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COUNSELOR) {
            return true
        }
        return application.userId == currentUser.userId
    }

    /**
     * Enforces privacy checks for student sensitive documents.
     * Students may ONLY access or modify documents belonging to their own userId.
     */
    fun canAccessDocument(document: StudentDocument): Boolean {
        if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COUNSELOR) {
            return true
        }
        return document.userId == currentUser.userId
    }

    /**
     * Filters list of applications based on strict privacy rules.
     */
    fun filterPrivateApplications(applications: List<Application>): List<Application> {
        if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COUNSELOR) {
            return applications
        }
        return applications.filter { it.userId == currentUser.userId }
    }

    /**
     * Filters list of sensitive documents based on strict privacy rules.
     */
    fun filterPrivateDocuments(documents: List<StudentDocument>): List<StudentDocument> {
        if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COUNSELOR) {
            return documents
        }
        return documents.filter { it.userId == currentUser.userId }
    }

    /**
     * Declarative Firestore Security Rules for deployment on Firebase console or Firebase CLI.
     * Enforces document-level security at the database engine level.
     */
    val FIRESTORE_SECURITY_RULES = """
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            
            // Helper functions
            function isSignedIn() {
              return request.auth != null;
            }
            
            function isOwner(userId) {
              return isSignedIn() && request.auth.uid == userId;
            }
            
            function isAdmin() {
              return isSignedIn() && 
                get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN';
            }
            
            // Users Collection
            match /users/{userId} {
              allow read: if isSignedIn();
              allow write: if isOwner(userId) || isAdmin();
            }
            
            // Universities, Scholarships, Countries, Programs, Announcements (Public Read, Admin Write)
            match /universities/{uniId} {
              allow read: if true;
              allow write: if isAdmin();
            }
            
            match /scholarships/{schId} {
              allow read: if true;
              allow write: if isAdmin();
            }
            
            match /countries/{countryId} {
              allow read: if true;
              allow write: if isAdmin();
            }
            
            match /programs/{progId} {
              allow read: if true;
              allow write: if isAdmin();
            }
            
            match /announcements/{annId} {
              allow read: if true;
              allow write: if isAdmin();
            }
            
            // Applications (Student Private Access)
            match /applications/{appId} {
              allow read: if isSignedIn() && (resource.data.userId == request.auth.uid || isAdmin());
              allow create: if isSignedIn() && request.resource.data.userId == request.auth.uid;
              allow update, delete: if isSignedIn() && (resource.data.userId == request.auth.uid || isAdmin());
            }
            
            // Documents (Student Private & Encrypted)
            match /documents/{docId} {
              allow read: if isSignedIn() && (resource.data.userId == request.auth.uid || isAdmin());
              allow create: if isSignedIn() && request.resource.data.userId == request.auth.uid;
              allow update, delete: if isSignedIn() && (resource.data.userId == request.auth.uid || isAdmin());
            }
            
            // Referrals
            match /referrals/{refId} {
              allow read, write: if isSignedIn() && (resource.data.referrerUserId == request.auth.uid || isAdmin());
            }
          }
        }
    """.trimIndent()
}
