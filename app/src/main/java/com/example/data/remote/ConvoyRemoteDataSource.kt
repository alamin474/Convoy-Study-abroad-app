package com.example.data.remote

import com.example.data.model.*
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

interface RemoteDataSource {
    fun fetchUniversities(): Flow<List<University>>
    fun fetchAllUniversitiesForAdmin(): Flow<List<University>>
    fun fetchUniversityById(id: String): Flow<University?>
    fun saveUniversity(university: University)
    fun deleteUniversity(universityId: String)
    fun updateUniversityStatus(universityId: String, status: EntityStatus)
    
    fun fetchScholarships(): Flow<List<Scholarship>>
    fun fetchAllScholarshipsForAdmin(): Flow<List<Scholarship>>
    fun fetchScholarshipById(id: String): Flow<Scholarship?>
    fun saveScholarship(scholarship: Scholarship)
    fun deleteScholarship(scholarshipId: String)
    fun updateScholarshipStatus(scholarshipId: String, status: EntityStatus)
    
    fun fetchCountries(): Flow<List<Country>>
    fun fetchAllCountriesForAdmin(): Flow<List<Country>>
    fun fetchCountryById(id: String): Flow<Country?>
    fun saveCountry(country: Country)
    fun deleteCountry(countryId: String)
    fun updateCountryStatus(countryId: String, status: EntityStatus)
    fun fetchPrograms(universityId: String? = null): Flow<List<Program>>
    
    fun fetchApplications(userId: String): Flow<List<Application>>
    fun createApplication(application: Application)
    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String = "")
    fun updateApplicationInternalNotes(applicationId: String, notes: String)
    fun requestMissingDocuments(applicationId: String, missingDocs: List<String>)
    fun submitApplication(applicationId: String)
    fun saveDraftApplication(application: Application)
    fun withdrawApplication(applicationId: String)
    
    fun fetchDocuments(userId: String): Flow<List<StudentDocument>>
    fun addDocument(document: StudentDocument)
    fun deleteDocument(documentId: String)
    
    fun fetchAnnouncements(): Flow<List<Announcement>>
    fun fetchReferrals(userId: String): Flow<List<Referral>>
    fun applyReferralCode(code: String, referredUserId: String, referredName: String, referredEmail: String): Pair<Boolean, String>
    fun updateReferralStatus(referralId: String, newStatus: ReferralStatus, adminNote: String)
    fun getUserReferralCode(userId: String): String

    // Lead generation methods
    fun fetchLeads(): Flow<List<Lead>>
    fun createLead(lead: Lead): Pair<Boolean, String>
    fun updateLeadStatus(leadId: String, status: LeadStatus, notes: String = "")

    // Requirements methods
    fun fetchRequirements(universityId: String? = null, programName: String? = null, intakeSeason: String? = null): Flow<List<UniversityRequirement>>
    fun fetchAllRequirementsForAdmin(): Flow<List<UniversityRequirement>>
    fun saveRequirement(requirement: UniversityRequirement)
    fun deleteRequirement(requirementId: String)
    fun updateRequirementStatus(requirementId: String, isPublished: Boolean)

    // Assistance Request methods
    fun fetchAssistanceRequests(userId: String? = null): Flow<List<AssistanceRequest>>
    fun fetchAllAssistanceRequestsForAdmin(): Flow<List<AssistanceRequest>>
    fun createAssistanceRequest(request: AssistanceRequest)
    fun updateAssistanceStatus(requestId: String, status: AssistanceStatus, counselor: String = "", internalNotes: String = "")
    fun addGuidanceMessage(requestId: String, message: GuidanceMessage)

    // Partner Management methods
    fun fetchPartners(): Flow<List<Partner>>
    fun savePartner(partner: Partner)
    fun deletePartner(partnerId: String)
    fun updatePartnerStatus(partnerId: String, status: PartnershipStatus)
    fun updateApplicationAttribution(
        applicationId: String,
        partnerId: String?,
        partnerName: String?,
        source: String,
        commissionEligible: Boolean,
        commissionStatus: CommissionStatus,
        commissionAmount: String? = null
    )

    // Sponsored Listing methods
    fun fetchSponsoredListings(): Flow<List<SponsoredListing>>
    fun saveSponsoredListing(listing: SponsoredListing)
    fun deleteSponsoredListing(listingId: String)
    fun updateSponsoredListingStatus(listingId: String, status: ListingStatus)

    // Analytics & Tracking methods
    fun trackAnalyticsEvent(event: AnalyticsEvent)
    fun fetchAnalyticsEvents(): Flow<List<AnalyticsEvent>>

    // Support & Contact methods
    fun fetchSupportRequests(userId: String? = null): Flow<List<SupportRequest>>
    fun fetchAllSupportRequestsForAdmin(): Flow<List<SupportRequest>>
    fun createSupportRequest(request: SupportRequest): Pair<Boolean, String>
    fun addSupportReply(requestId: String, reply: SupportReply, newStatus: SupportStatus? = null)
    fun updateSupportStatus(requestId: String, status: SupportStatus, internalNotes: String = "", assignedStaff: String = "")
    fun fetchSupportConfig(): Flow<SupportConfig>
    fun updateSupportConfig(config: SupportConfig)

    // Chat & Counsellor Hub methods
    fun fetchConversations(userId: String): Flow<List<ChatConversation>>
    fun fetchAllConversationsForAdmin(): Flow<List<ChatConversation>>
    fun fetchConversationById(conversationId: String): Flow<ChatConversation?>
    fun fetchMessages(conversationId: String): Flow<List<ChatMessage>>
    fun fetchInternalNotes(conversationId: String): Flow<List<InternalNote>>
    fun sendMessage(message: ChatMessage)
    fun markConversationAsRead(conversationId: String, userId: String)
    fun createOrGetCounsellorConversation(userId: String, userName: String, userEmail: String): String
    fun createOrGetSupportConversation(userId: String, userName: String, userEmail: String, topic: String): String
    fun createOrGetApplicationConversation(userId: String, userName: String, userEmail: String, applicationId: String, universityName: String, programName: String): String
    fun addInternalNote(note: InternalNote)
    fun updateConversationStatus(conversationId: String, status: ConversationStatus)
    fun assignConversationCounsellor(conversationId: String, counsellorId: String, counsellorName: String)
    fun deleteChatMessage(conversationId: String, messageId: String)
    fun reportOrBlockConversation(conversationId: String, isReported: Boolean, isBlocked: Boolean)
    fun getCounsellors(): List<CounsellorProfile>

    // Admin methods
    fun fetchAllApplicationsForAdmin(): Flow<List<Application>>
    fun fetchAllDocumentsForAdmin(): Flow<List<StudentDocument>>
    fun fetchStudents(): Flow<List<User>>
    fun fetchAllReferralsForAdmin(): Flow<List<Referral>>
    fun fetchRecentActivities(): Flow<List<RecentActivity>>
}

class ConvoyRemoteDataSource : RemoteDataSource {

    private val universitiesState = MutableStateFlow<List<University>>(
        listOf(
            University(
                universityId = "uni_1",
                name = "University of Oxford",
                country = "United Kingdom",
                city = "Oxford",
                ranking = 1,
                tuitionFee = "$32,000 USD / year",
                applicationFee = "$100 USD",
                description = "The University of Oxford is a collegiate research university in Oxford, England. Oldest university in the English-speaking world with world-renowned academic leadership.",
                programs = listOf("MSc Computer Science", "PPE", "Medicine", "MBA"),
                intakes = listOf("Fall 2026", "Spring 2027"),
                admissionRequirements = listOf(
                    "High School Diploma or Bachelor's Degree with A* / First-Class Honors",
                    "IELTS Overall 7.5 or TOEFL iBT 110",
                    "Statement of Purpose & 2 Academic Recommendation Letters"
                ),
                englishRequirements = "IELTS 7.5 Overall (min 7.0 per component)",
                scholarships = listOf("Chevening UK Scholarship", "Clarendon Fund", "Rhodes Scholarship"),
                campusImages = listOf("https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800&q=80"),
                officialWebsite = "https://www.ox.ac.uk",
                applicationUrl = "https://apply.convoy.edu/oxford",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇬🇧",
                logoUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=300&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800&q=80",
                acceptanceRatePercent = 17,
                isFeatured = true,
                isBookmarked = true
            ),
            University(
                universityId = "uni_2",
                name = "Technical University of Munich (TUM)",
                country = "Germany",
                city = "Munich",
                ranking = 28,
                tuitionFee = "€3,000 EUR / year",
                applicationFee = "€75 EUR",
                description = "Technical University of Munich is one of Europe's top universities committed to excellence in research and technology.",
                programs = listOf("Robotics & AI", "Mechanical Engineering", "Data Engineering", "Informatics"),
                intakes = listOf("Winter 2026/27", "Summer 2027"),
                admissionRequirements = listOf(
                    "Recognized Bachelor's Degree in STEM",
                    "English proficiency (IELTS 6.5+) or German B2",
                    "Aptitude assessment test or technical interview"
                ),
                englishRequirements = "IELTS 6.5+ / TOEFL 88+",
                scholarships = listOf("DAAD Master's Scholarship", "TUM Foundation Grant"),
                campusImages = listOf("https://images.unsplash.com/photo-1592280771190-3e2e4d571952?w=800&q=80"),
                officialWebsite = "https://www.tum.de",
                applicationUrl = "https://apply.convoy.edu/tum",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇩🇪",
                logoUrl = "https://images.unsplash.com/photo-1592280771190-3e2e4d571952?w=300&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1592280771190-3e2e4d571952?w=800&q=80",
                acceptanceRatePercent = 28,
                isFeatured = true,
                isBookmarked = false
            ),
            University(
                universityId = "uni_3",
                name = "University of Toronto",
                country = "Canada",
                city = "Toronto",
                ranking = 21,
                tuitionFee = "$22,000 USD / year",
                applicationFee = "$125 CAD",
                description = "Leading institution in Canada offering world-class research and academic programs across three vibrant campuses.",
                programs = listOf("Data Science", "Finance", "Biomedical Engineering", "Architecture"),
                intakes = listOf("Fall 2026"),
                admissionRequirements = listOf(
                    "Bachelor's degree with minimum B+ average",
                    "IELTS 7.0 or TOEFL 100",
                    "2 Professional or Academic References"
                ),
                englishRequirements = "IELTS 7.0 Overall",
                scholarships = listOf("Lester B. Pearson International Scholarship"),
                campusImages = listOf("https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80"),
                officialWebsite = "https://www.utoronto.ca",
                applicationUrl = "https://apply.convoy.edu/utoronto",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇨🇦",
                logoUrl = "https://images.unsplash.com/photo-1562774053-701939374585?w=300&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80",
                acceptanceRatePercent = 43,
                isFeatured = true,
                isBookmarked = true
            ),
            University(
                universityId = "uni_4",
                name = "University of Melbourne",
                country = "Australia",
                city = "Melbourne",
                ranking = 14,
                tuitionFee = "$24,000 USD / year",
                applicationFee = "$100 AUD",
                description = "Australia's top ranked university known for global research impact and heritage campus precincts.",
                programs = listOf("Business Administration", "Law", "Public Health", "Software Engineering"),
                intakes = listOf("Semester 1 2026", "Semester 2 2026"),
                admissionRequirements = listOf(
                    "Undergraduate degree equivalent to Australian Bachelor",
                    "IELTS 6.5 overall",
                    "Academic Transcripts & Statement"
                ),
                englishRequirements = "IELTS 6.5 (no band below 6.0)",
                scholarships = listOf("Australia Awards", "Melbourne International Undergraduate Scholarship"),
                campusImages = listOf("https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=800&q=80"),
                officialWebsite = "https://www.unimelb.edu.au",
                applicationUrl = "https://apply.convoy.edu/unimelb",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇦🇺",
                logoUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=300&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=800&q=80",
                acceptanceRatePercent = 35,
                isFeatured = false,
                isBookmarked = false
            ),
            University(
                universityId = "uni_5",
                name = "ETH Zurich",
                country = "Switzerland",
                city = "Zurich",
                ranking = 7,
                tuitionFee = "CHF 1,500 / year",
                applicationFee = "CHF 150",
                description = "ETH Zurich is a premier European technology university specializing in cutting-edge science and engineering research.",
                programs = listOf("Quantum Engineering", "Physics", "Cyber Security", "Civil Engineering"),
                intakes = listOf("Autumn 2026"),
                admissionRequirements = listOf(
                    "Excellent Bachelor's degree in relevant discipline",
                    "Proof of English proficiency (C1 level)",
                    "GRE General Test for non-Bologna degrees"
                ),
                englishRequirements = "TOEFL 100 / IELTS 7.0+",
                scholarships = listOf("Excellence Scholarship & Opportunity Programme (ESOP)"),
                campusImages = listOf("https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800&q=80"),
                officialWebsite = "https://ethz.ch",
                applicationUrl = "https://apply.convoy.edu/ethz",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇨🇭",
                logoUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=300&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800&q=80",
                acceptanceRatePercent = 20,
                isFeatured = true,
                isLowTuition = true,
                isBookmarked = false
            ),
            University(
                universityId = "uni_cyprus_1",
                name = "University of Nicosia",
                country = "Cyprus",
                city = "Nicosia",
                ranking = 501,
                tuitionFee = "€3,200 / year",
                applicationFee = "€55",
                description = "Largest university in Cyprus offering English medium degrees in Business, Medicine, and Computer Science with up to 50% tuition scholarship.",
                programs = listOf("BSc Computer Science", "MBA International", "MSc Blockchain Technology", "MD Medicine"),
                intakes = listOf("Fall 2026", "Spring 2027"),
                admissionRequirements = listOf("High School Diploma / Bachelor transcript", "IELTS 6.0 or MOI Certificate"),
                englishRequirements = "IELTS 6.0 / MOI accepted",
                scholarships = listOf("50% Merit Tuition Waiver", "International Student Bursary"),
                officialWebsite = "https://www.unic.ac.cy",
                applicationUrl = "https://apply.convoy.edu/unic",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇨🇾",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_malaysia_1",
                name = "Universiti Malaya (UM)",
                country = "Malaysia",
                city = "Kuala Lumpur",
                ranking = 60,
                tuitionFee = "$3,100 / year",
                applicationFee = "$50 USD",
                description = "Malaysia's premier top-ranking public university providing English-taught engineering, IT, and medical programs.",
                programs = listOf("BSc Software Engineering", "Master of Data Science", "MBA", "MSc Artificial Intelligence"),
                intakes = listOf("October 2026", "March 2027"),
                admissionRequirements = listOf("Bachelor's GPA 3.0+", "IELTS 6.0 or TOEFL 80"),
                englishRequirements = "IELTS 6.0 / TOEFL 80",
                scholarships = listOf("MIS Malaysian International Scholarship", "UM Graduate Fellowship"),
                officialWebsite = "https://www.um.edu.my",
                applicationUrl = "https://apply.convoy.edu/um",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇲🇾",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_greece_1",
                name = "National and Kapodistrian University of Athens",
                country = "Greece",
                city = "Athens",
                ranking = 250,
                tuitionFee = "€1,800 / year",
                applicationFee = "€0",
                description = "Greece's oldest public university offering fully English-taught Bachelor and Master programs in Archaeology, Medicine, and IT.",
                programs = listOf("BA Archaeology & History", "MSc Applied Computer Science", "MSc Medical Physics"),
                intakes = listOf("September 2026"),
                admissionRequirements = listOf("Higher Secondary Certificate / Bachelor degree", "IELTS 6.0+"),
                englishRequirements = "IELTS 6.0 / Cambridge B2",
                scholarships = listOf("IKY Greek State Scholarship", "University Excellence Waiver"),
                officialWebsite = "https://en.uoa.gr",
                applicationUrl = "https://apply.convoy.edu/uoa",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇬🇷",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_malta_1",
                name = "University of Malta",
                country = "Malta",
                city = "Msida",
                ranking = 601,
                tuitionFee = "€3,800 / year",
                applicationFee = "€95",
                description = "Historic European public university in Malta providing accredited English degrees with full EU portability.",
                programs = listOf("MSc Artificial Intelligence", "BSc Information Technology", "MA International Relations"),
                intakes = listOf("October 2026", "February 2027"),
                admissionRequirements = listOf("Bachelors in relevant field", "IELTS 6.5 Overall"),
                englishRequirements = "IELTS 6.5 / TOEFL iBT 80",
                scholarships = listOf("Get Qualified Scheme", "Endeavour II Scholarship"),
                officialWebsite = "https://www.um.edu.mt",
                applicationUrl = "https://apply.convoy.edu/umalta",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇲🇹",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_spain_1",
                name = "Autonomous University of Barcelona (UAB)",
                country = "Spain",
                city = "Barcelona",
                ranking = 149,
                tuitionFee = "€2,200 / year",
                applicationFee = "€30",
                description = "Top Spanish public university located in Barcelona, offering world-recognized low tuition Master degrees in English.",
                programs = listOf("MSc Data Science", "Master in Logistics", "MA International Relations"),
                intakes = listOf("September 2026"),
                admissionRequirements = listOf("Recognized Bachelor's degree", "IELTS 6.5 or MOI"),
                englishRequirements = "IELTS 6.5 / B2 Certificate",
                scholarships = listOf("MAEC-AECID Spanish Government Grant"),
                officialWebsite = "https://www.uab.cat",
                applicationUrl = "https://apply.convoy.edu/uab",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇪🇸",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_croatia_1",
                name = "University of Zagreb",
                country = "Croatia",
                city = "Zagreb",
                ranking = 701,
                tuitionFee = "€2,500 / year",
                applicationFee = "€40",
                description = "Leading Croatian public university with comprehensive English programs in Medicine, Engineering, and Business.",
                programs = listOf("Doctor of Medicine (MD)", "BSc Electrical Engineering", "MBA International"),
                intakes = listOf("October 2026"),
                admissionRequirements = listOf("High School Diploma / Bachelor degree", "English proficiency B2"),
                englishRequirements = "IELTS 6.0 or B2 Certificate",
                scholarships = listOf("Croatian Ministry Bilateral Grant"),
                officialWebsite = "https://www.unizg.hr",
                applicationUrl = "https://apply.convoy.edu/unizg",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇭🇷",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_italy_1",
                name = "Sapienza University of Rome",
                country = "Italy",
                city = "Rome",
                ranking = 134,
                tuitionFee = "€1,000 / year",
                applicationFee = "€30",
                description = "Europe's largest public university offering top ranked English Master programs in AI, Finance, and Architecture at under €1,500/yr.",
                programs = listOf("MSc Artificial Intelligence & Robotics", "MSc Finance & Development", "MSc Architecture"),
                intakes = listOf("September 2026"),
                admissionRequirements = listOf("Bachelor's degree in STEM/Business", "IELTS 6.0+"),
                englishRequirements = "IELTS 6.0 / B2 English Certificate",
                scholarships = listOf("DSU Lazio Need-Based Regional Waiver (100% Free Tuition + €6,000 Housing)"),
                officialWebsite = "https://www.uniroma1.it",
                applicationUrl = "https://apply.convoy.edu/sapienza",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇮🇹",
                isFeatured = true,
                isLowTuition = true
            ),
            University(
                universityId = "uni_lithuania_1",
                name = "Vilnius University",
                country = "Lithuania",
                city = "Vilnius",
                ranking = 400,
                tuitionFee = "€2,800 / year",
                applicationFee = "€100",
                description = "Historic European Baltic university offering affordable tech, finance, and medical programs in English.",
                programs = listOf("MSc Information Systems", "BSc Global Business", "MD General Medicine"),
                intakes = listOf("September 2026", "February 2027"),
                admissionRequirements = listOf("Bachelor degree transcript", "IELTS 5.5+"),
                englishRequirements = "IELTS 5.5 / TOEFL 65 / MOI",
                scholarships = listOf("Lithuanian State Master's Scholarship"),
                officialWebsite = "https://www.vu.lt",
                applicationUrl = "https://apply.convoy.edu/vu",
                status = EntityStatus.PUBLISHED,
                flagEmoji = "🇱🇹",
                isFeatured = true,
                isLowTuition = true
            )
        )
    )

    private val scholarshipsState = MutableStateFlow<List<Scholarship>>(
        listOf(
            Scholarship(
                scholarshipId = "sch_1",
                name = "Chevening International Scholarship",
                country = "United Kingdom",
                university = "UK Foreign, Commonwealth & Development Office",
                degreeLevel = "Master's Degree",
                scholarshipType = "Government / Fully Funded",
                fundingDetails = "100% Tuition + £1,400/mo Stipend + Flights",
                eligibility = listOf(
                    "Be a citizen of a Chevening-eligible country",
                    "Have an undergraduate degree enabling UK postgraduate entry",
                    "Minimum 2 years work experience"
                ),
                deadline = "03 November 2026",
                requiredDocuments = listOf("Passport", "Transcripts", "2 References", "Essay Answers"),
                officialWebsite = "https://www.chevening.org",
                applicationUrl = "https://apply.convoy.edu/scholarships/chevening",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                logoUrl = "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=300&q=80",
                description = "Chevening is the UK Government’s global scholarship programme providing fully-funded Master's awards.",
                isFeatured = true,
                isSaved = true
            ),
            Scholarship(
                scholarshipId = "sch_2",
                name = "DAAD Master's Scholarships for All Disciplines",
                country = "Germany",
                university = "German Academic Exchange Service (DAAD)",
                degreeLevel = "Master's Degree",
                scholarshipType = "Government",
                fundingDetails = "€934/mo Stipend + Health Insurance + Travel Allowance",
                eligibility = listOf(
                    "Graduated with Bachelor's degree within last 6 years",
                    "Minimum GPA 3.0 equivalent",
                    "Language proficiency certificate"
                ),
                deadline = "15 October 2026",
                requiredDocuments = listOf("DAAD Application Form", "CV Europass", "Motivation Letter", "Transcripts"),
                officialWebsite = "https://www.daad.de",
                applicationUrl = "https://apply.convoy.edu/scholarships/daad",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                logoUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=300&q=80",
                description = "DAAD offers postgraduate scholarships for international graduates to complete Master's study in Germany.",
                isFeatured = true,
                isSaved = false
            ),
            Scholarship(
                scholarshipId = "sch_3",
                name = "Fulbright Foreign Student Program",
                country = "United States",
                university = "U.S. Department of State",
                degreeLevel = "Master's & PhD",
                scholarshipType = "Fully Funded",
                fundingDetails = "Full Tuition + Living Expenses + Health Coverage",
                eligibility = listOf(
                    "Completed 4-year Bachelor's degree",
                    "High academic performance & leadership potential",
                    "TOEFL iBT 90+ / IELTS 7.0+"
                ),
                deadline = "15 October 2026",
                requiredDocuments = listOf("U.S. Embassy Application Form", "Personal Statement", "3 Recommendation Letters"),
                officialWebsite = "https://foreign.fulbrightonline.org",
                applicationUrl = "https://apply.convoy.edu/scholarships/fulbright",
                lastVerified = "2026-02-01",
                status = EntityStatus.PUBLISHED,
                logoUrl = "https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?w=300&q=80",
                description = "Enables graduate students and young professionals to study and conduct research in the U.S.",
                isFeatured = true,
                isSaved = true
            )
        )
    )

    private val countriesState = MutableStateFlow<List<Country>>(
        listOf(
            Country(
                countryId = "c_cyprus",
                name = "Cyprus",
                flagEmoji = "🇨🇾",
                universityCount = 14,
                avgTuitionPerYear = "€2,500 - €5,500 / year",
                popularCities = listOf("Nicosia", "Limassol", "Larnaca"),
                imageUrl = "https://images.unsplash.com/photo-1548013146-72479768bada?w=600&q=80",
                overview = "Cyprus is a fast-growing Mediterranean higher education hub offering high quality European degrees at affordable tuition rates.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD", "Diploma"),
                tuitionRange = "€2,500 - €6,000 / year",
                livingCostOverview = "€450 - €700 / month",
                scholarshipAvailability = "High (50% merit scholarships available)",
                studentVisaOverview = "Easy visa process with high approval rates for international students.",
                partTimeWorkInfo = "Up to 20 hours/week permitted after 6 months of study.",
                popularFields = listOf("Business Administration", "Hospitality Management", "Computer Science", "Medicine"),
                applicationInfo = "Direct application via Convoy or university admissions portal.",
                officialWebsite = "https://www.highereducation.ac.cy",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_malaysia",
                name = "Malaysia",
                flagEmoji = "🇲🇾",
                universityCount = 38,
                avgTuitionPerYear = "$2,500 - $6,000 / year",
                popularCities = listOf("Kuala Lumpur", "Penang", "Johor Bahru"),
                imageUrl = "https://images.unsplash.com/photo-1596422846543-75c6fc197f07?w=600&q=80",
                overview = "Malaysia hosts world-class branch campuses from UK and Australian universities at a fraction of Western study costs.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "$2,500 - $7,000 / year",
                livingCostOverview = "$350 - $600 / month",
                scholarshipAvailability = "High (MIS & University Merit Grants)",
                studentVisaOverview = "Student Pass issued by EMGS with streamlined online processing.",
                partTimeWorkInfo = "Allowed up to 20 hours/week during semester breaks.",
                popularFields = listOf("Engineering", "Information Technology", "Business", "Pharmacy"),
                applicationInfo = "Apply directly or via EMGS portal with Convoy counselor support.",
                officialWebsite = "https://educationmalaysia.gov.my",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_denmark",
                name = "Denmark",
                flagEmoji = "🇩🇰",
                universityCount = 18,
                avgTuitionPerYear = "€6,000 - €12,000 / year",
                popularCities = listOf("Copenhagen", "Aarhus", "Odense"),
                imageUrl = "https://images.unsplash.com/photo-1513622470522-26c3c8a854bc?w=600&q=80",
                overview = "Denmark offers innovative problem-based learning, high standard of living, and top ranking Nordic research universities.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€6,000 - €14,000 / year",
                livingCostOverview = "€800 - €1,200 / month",
                scholarshipAvailability = "Danish Government Scholarships for Non-EU/EEA students.",
                studentVisaOverview = "ST1 Residence permit required for international higher education.",
                partTimeWorkInfo = "20 hours/week allowed during study, 37 hours/week in June, July & August.",
                popularFields = listOf("Renewable Energy", "Design", "Computer Science", "Biotechnology"),
                applicationInfo = "Centralized portalOptagelse.dk or university online portal.",
                officialWebsite = "https://studyindenmark.dk",
                isFeatured = true,
                isLowTuitionDestination = false
            ),
            Country(
                countryId = "c_greece",
                name = "Greece",
                flagEmoji = "🇬🇷",
                universityCount = 24,
                avgTuitionPerYear = "€1,500 - €4,500 / year",
                popularCities = listOf("Athens", "Thessaloniki", "Crete"),
                imageUrl = "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=600&q=80",
                overview = "Greece combines rich ancient heritage with modern English-taught degree programs and very low living expenses.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€1,500 - €5,000 / year",
                livingCostOverview = "€400 - €700 / month",
                scholarshipAvailability = "State Scholarships Foundation (IKY) & Ministry Grants.",
                studentVisaOverview = "Type D National Visa for study required with embassy appointment.",
                partTimeWorkInfo = "Up to 20 hours/week permitted for residence permit holders.",
                popularFields = listOf("Archaeology", "Maritime Studies", "Medicine", "International Business"),
                applicationInfo = "Ministry of Education application portal or university direct entry.",
                officialWebsite = "https://studyingreece.edu.gr",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_uk",
                name = "United Kingdom",
                flagEmoji = "🇬🇧",
                universityCount = 140,
                avgTuitionPerYear = "£12,000 - £25,000 / year",
                popularCities = listOf("London", "Oxford", "Cambridge", "Manchester", "Edinburgh"),
                imageUrl = "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=600&q=80",
                overview = "Home to Oxford, Cambridge and world-leading research, the UK offers 1-year Master degrees and Graduate Route work visas.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "£10,000 - £30,000 / year",
                livingCostOverview = "£900 - £1,400 / month",
                scholarshipAvailability = "Chevening, Commonwealth, Great Scholarships, University Merit Awards.",
                studentVisaOverview = "Student Route Visa (CAS points-based system) with 2-year post-study work visa.",
                partTimeWorkInfo = "20 hours/week during term time, full-time during official vacation.",
                popularFields = listOf("Computer Science", "Business & Finance", "Engineering", "Law"),
                applicationInfo = "UCAS for undergraduate, direct university portal for postgraduate.",
                officialWebsite = "https://study-uk.britishcouncil.org",
                isFeatured = true,
                isLowTuitionDestination = false
            ),
            Country(
                countryId = "c_usa",
                name = "United States",
                flagEmoji = "🇺🇸",
                universityCount = 4000,
                avgTuitionPerYear = "$15,000 - $45,000 / year",
                popularCities = listOf("New York", "Boston", "San Francisco", "Chicago", "Los Angeles"),
                imageUrl = "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?w=600&q=80",
                overview = "The United States hosts the world's highest density of top 100 universities, offering unparalleled research facilities and OPT work authorization.",
                popularStudyLevels = listOf("Associate", "Bachelor's", "Master's", "PhD"),
                tuitionRange = "$12,000 - $55,000 / year",
                livingCostOverview = "$1,000 - $1,800 / month",
                scholarshipAvailability = "Fulbright, Graduate Assistantships, Athletic & Merit Waivers.",
                studentVisaOverview = "F-1 Student Visa with SEVIS fee and embassy interview.",
                partTimeWorkInfo = "Up to 20 hours/week on-campus during academic sessions.",
                popularFields = listOf("Artificial Intelligence", "Data Science", "Business Administration", "Biomedical Engineering"),
                applicationInfo = "Common App for undergrad, direct department portal for graduate.",
                officialWebsite = "https://educationusa.state.gov",
                isFeatured = true,
                isLowTuitionDestination = false
            ),
            Country(
                countryId = "c_australia",
                name = "Australia",
                flagEmoji = "🇦🇺",
                universityCount = 43,
                avgTuitionPerYear = "AUD $20,000 - $38,000 / year",
                popularCities = listOf("Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide"),
                imageUrl = "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?w=600&q=80",
                overview = "Australia provides high quality of life, Group of Eight research institutions, and post-study work rights up to 4-6 years.",
                popularStudyLevels = listOf("Diploma", "Bachelor's", "Master's", "PhD"),
                tuitionRange = "AUD $18,000 - $42,000 / year",
                livingCostOverview = "AUD $1,200 - $1,800 / month",
                scholarshipAvailability = "Australia Awards, Destination Australia, University Merit Bursaries.",
                studentVisaOverview = "Subclass 500 Student Visa with Genuine Student (GS) assessment.",
                partTimeWorkInfo = "Up to 48 hours per fortnight when course is in session.",
                popularFields = listOf("Cybersecurity", "Nursing & Healthcare", "Civil Engineering", "Accounting"),
                applicationInfo = "Direct application or authorized education agent submission.",
                officialWebsite = "https://www.studyinaustralia.gov.au",
                isFeatured = true,
                isLowTuitionDestination = false
            ),
            Country(
                countryId = "c_malta",
                name = "Malta",
                flagEmoji = "🇲🇹",
                universityCount = 12,
                avgTuitionPerYear = "€3,000 - €6,500 / year",
                popularCities = listOf("Valletta", "Msida", "Sliema"),
                imageUrl = "https://images.unsplash.com/photo-1516483638261-f4dbaf036963?w=600&q=80",
                overview = "Malta is an English-speaking EU island nation providing accredited European qualifications, warm Mediterranean weather, and low tuition.",
                popularStudyLevels = listOf("Certificate", "Bachelor's", "Master's"),
                tuitionRange = "€3,000 - €7,000 / year",
                livingCostOverview = "€500 - €850 / month",
                scholarshipAvailability = "Get Qualified Scheme & Endeavour Scholarships.",
                studentVisaOverview = "Schengen National Visa Type D with easy travel across Europe.",
                partTimeWorkInfo = "20 hours/week permitted after 90 days of arrival.",
                popularFields = listOf("Tourism & Event Management", "Software Engineering", "Finance", "Blockchain"),
                applicationInfo = "Direct online submission to University of Malta or partner colleges.",
                officialWebsite = "https://education.gov.mt",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_spain",
                name = "Spain",
                flagEmoji = "🇪🇸",
                universityCount = 82,
                avgTuitionPerYear = "€1,800 - €5,000 / year",
                popularCities = listOf("Madrid", "Barcelona", "Valencia", "Seville"),
                imageUrl = "https://images.unsplash.com/photo-1543783207-ec64e4d95325?w=600&q=80",
                overview = "Spain offers affordable living, premier business schools, and low public university tuition for international candidates.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€1,200 - €6,000 / year",
                livingCostOverview = "€550 - €900 / month",
                scholarshipAvailability = "MAEC-AECID Scholarships, Spanish Government Grants.",
                studentVisaOverview = "Spanish Student Visa (Type D) granting Schengen mobility.",
                partTimeWorkInfo = "Up to 30 hours/week compatible with study schedule.",
                popularFields = listOf("International Business", "Architecture", "Spanish Language", "Data Science"),
                applicationInfo = "UNEDasiss for undergraduate credential verification, direct for Master's.",
                officialWebsite = "https://www.sepie.es",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_croatia",
                name = "Croatia",
                flagEmoji = "🇭🇷",
                universityCount = 15,
                avgTuitionPerYear = "€2,000 - €4,500 / year",
                popularCities = listOf("Zagreb", "Split", "Rijeka", "Dubrovnik"),
                imageUrl = "https://images.unsplash.com/photo-1516483638261-f4dbaf036963?w=600&q=80",
                overview = "Croatia provides safe Schengen European study with low tuition fees, growing English programs, and beautiful coastal campuses.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€1,800 - €5,000 / year",
                livingCostOverview = "€400 - €700 / month",
                scholarshipAvailability = "Croatian Government Bilateral Scholarships.",
                studentVisaOverview = "Schengen Visa Type D issued through VFS Global / Embassy.",
                partTimeWorkInfo = "Student Service (Studentski Servis) allows flexible part-time work.",
                popularFields = listOf("Medicine & Dental", "Computer Engineering", "Tourism", "Economics"),
                applicationInfo = "Direct application to University of Zagreb or partner institutions.",
                officialWebsite = "https://www.studyincroatia.hr",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_germany",
                name = "Germany",
                flagEmoji = "🇩🇪",
                universityCount = 380,
                avgTuitionPerYear = "€0 - €3,000 / year",
                popularCities = listOf("Munich", "Berlin", "Heidelberg", "Frankfurt", "Aachen"),
                imageUrl = "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?w=600&q=80",
                overview = "Germany is famous for tuition-free or near-zero tuition public universities, world-class engineering, and 18-month job seeker visas.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€0 - €3,500 / year",
                livingCostOverview = "€850 - €1,100 / month",
                scholarshipAvailability = "DAAD Grants, Deutschlandstipendium, Heinrich Böll Grants.",
                studentVisaOverview = "National Visa Type D with Blocked Account (Sperrkonto) proof of funds.",
                partTimeWorkInfo = "140 full days or 280 half days per calendar year.",
                popularFields = listOf("Automotive Engineering", "Robotics & AI", "Physics", "International Finance"),
                applicationInfo = "Uni-assist portal or direct university portal.",
                officialWebsite = "https://www.study-in-germany.de",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_italy",
                name = "Italy",
                flagEmoji = "🇮🇹",
                universityCount = 95,
                avgTuitionPerYear = "€900 - €4,000 / year",
                popularCities = listOf("Rome", "Milan", "Bologna", "Turin", "Florence"),
                imageUrl = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=600&q=80",
                overview = "Italy features Europe's oldest universities, low public university fees based on family income, and DSU regional scholarships.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€900 - €4,500 / year",
                livingCostOverview = "€600 - €1,000 / month",
                scholarshipAvailability = "DSU Regional Need-Based Scholarships (100% tuition waiver + free housing).",
                studentVisaOverview = "Universitaly portal pre-enrollment & Italian Embassy Study Visa.",
                partTimeWorkInfo = "Up to 20 hours/week permitted for international students.",
                popularFields = listOf("Fashion & Design", "Automotive Design", "Biomedical Engineering", "Economics"),
                applicationInfo = "Universitaly portal pre-enrollment plus university evaluation portal.",
                officialWebsite = "https://www.universitaly.it",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_finland",
                name = "Finland",
                flagEmoji = "🇫🇮",
                universityCount = 38,
                avgTuitionPerYear = "€6,000 - €12,000 / year",
                popularCities = listOf("Helsinki", "Tampere", "Turku", "Oulu"),
                imageUrl = "https://images.unsplash.com/photo-1538332576228-eb5b4c4de6f5?w=600&q=80",
                overview = "Finland boasts the world's top educational system, high safety standards, generous university scholarships, and post-study permanent residence options.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€5,000 - €13,000 / year",
                livingCostOverview = "€700 - €1,100 / month",
                scholarshipAvailability = "Finland Scholarship (100% tuition + €5,000 relocation grant).",
                studentVisaOverview = "Continuous Residence Permit (A-permit) granted for entire duration of study.",
                partTimeWorkInfo = "30 hours/week average permitted during academic term.",
                popularFields = listOf("Software Engineering", "Clean Energy", "Gaming & Education Tech", "Nursing"),
                applicationInfo = "Studyinfo.fi joint application system in January/February.",
                officialWebsite = "https://www.studyinfinland.fi",
                isFeatured = true,
                isLowTuitionDestination = false
            ),
            Country(
                countryId = "c_lithuania",
                name = "Lithuania",
                flagEmoji = "🇱🇹",
                universityCount = 19,
                avgTuitionPerYear = "€2,000 - €4,800 / year",
                popularCities = listOf("Vilnius", "Kaunas", "Klaipeda"),
                imageUrl = "https://images.unsplash.com/photo-1548013146-72479768bada?w=600&q=80",
                overview = "Lithuania is a vibrant Baltic tech hub providing affordable European degrees, modern labs, and 15-month post-grad job seeker visas.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "€1,800 - €5,000 / year",
                livingCostOverview = "€400 - €700 / month",
                scholarshipAvailability = "Lithuanian State Scholarships for Master's degree studies.",
                studentVisaOverview = "National Visa D / Temporary Residence Permit (TRP) via MIGRIS portal.",
                partTimeWorkInfo = "Up to 40 hours/week allowed for Master's students, 20 hrs for Bachelor's.",
                popularFields = listOf("Fintech & Banking", "Laser Physics", "Biotechnology", "Aviation Management"),
                applicationInfo = "Direct university application portal with Convoy document verification.",
                officialWebsite = "https://www.studyin.lt",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_japan",
                name = "Japan",
                flagEmoji = "🇯🇵",
                universityCount = 780,
                avgTuitionPerYear = "$3,500 - $8,000 / year",
                popularCities = listOf("Tokyo", "Kyoto", "Osaka", "Nagoya", "Fukuoka"),
                imageUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=600&q=80",
                overview = "Japan offers world-renowned technology innovation, MEXT government full scholarships, and expanding English degree programs.",
                popularStudyLevels = listOf("Associate", "Bachelor's", "Master's", "PhD"),
                tuitionRange = "$3,000 - $9,000 / year",
                livingCostOverview = "$700 - $1,200 / month",
                scholarshipAvailability = "MEXT Government Scholarship (100% tuition + stipend + flight) & JASSO.",
                studentVisaOverview = "Certificate of Eligibility (COE) issued by Immigration Services Agency.",
                partTimeWorkInfo = "Up to 28 hours/week with Work Permit stamp on arrival.",
                popularFields = listOf("Robotics", "Japanese Studies", "Civil Engineering", "International Relations"),
                applicationInfo = "Direct application to university international admissions office.",
                officialWebsite = "https://www.studyinjapan.go.jp",
                isFeatured = true,
                isLowTuitionDestination = true
            ),
            Country(
                countryId = "c_southkorea",
                name = "South Korea",
                flagEmoji = "🇰🇷",
                universityCount = 200,
                avgTuitionPerYear = "$3,000 - $7,500 / year",
                popularCities = listOf("Seoul", "Busan", "Incheon", "Daejeon"),
                imageUrl = "https://images.unsplash.com/photo-1538485399081-7191377e8241?w=600&q=80",
                overview = "South Korea provides cutting-edge tech education, Global Korea Scholarship (GKS) full awards, and strong corporate career pathways.",
                popularStudyLevels = listOf("Bachelor's", "Master's", "PhD"),
                tuitionRange = "$2,800 - $8,000 / year",
                livingCostOverview = "$650 - $1,100 / month",
                scholarshipAvailability = "Global Korea Scholarship (GKS) covering full tuition, stipend and living expenses.",
                studentVisaOverview = "D-2 Student Visa issued with university admission certificate.",
                partTimeWorkInfo = "Up to 20-25 hours/week after completing 1 semester.",
                popularFields = listOf("Semiconductor Engineering", "K-Culture & Media", "Global Business", "Computer Science"),
                applicationInfo = "Study in Korea central portal or direct university portal.",
                officialWebsite = "https://www.studyinkorea.go.kr",
                isFeatured = true,
                isLowTuitionDestination = true
            )
        )
    )

    private val programsState = MutableStateFlow<List<Program>>(
        listOf(
            Program("p_1", "uni_1", "University of Oxford", "MSc in Computer Science", "Master's Degree", "1 Year", "$32,000 USD", listOf("Fall 2026"), listOf("First Class Degree in CS or Math", "IELTS 7.5")),
            Program("p_2", "uni_2", "Technical University of Munich", "MSc Robotics, Cognition & Intelligence", "Master's Degree", "2 Years", "€3,000 EUR", listOf("Winter 2026/27"), listOf("BSc in STEM discipline", "IELTS 6.5"))
        )
    )

    private val applicationsState = MutableStateFlow<List<Application>>(
        listOf(
            Application(
                applicationId = "app_1",
                userId = "student_101",
                universityId = "uni_1",
                universityName = "University of Oxford",
                universityLogoUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=300&q=80",
                programId = "p_1",
                programName = "MSc in Computer Science",
                degreeLevel = "Master's Degree",
                intakeSeason = "Fall 2026",
                country = "United Kingdom",
                status = ApplicationStatus.IN_REVIEW,
                submittedDate = "12 January 2026",
                nextMilestone = "Awaiting Academic Admissions Board Interview Call",
                completionPercentage = 0.85f
            ),
            Application(
                applicationId = "app_2",
                userId = "student_101",
                universityId = "uni_2",
                universityName = "Technical University of Munich",
                universityLogoUrl = "https://images.unsplash.com/photo-1592280771190-3e2e4d571952?w=300&q=80",
                programId = "p_2",
                programName = "MSc Robotics, Cognition & Intelligence",
                degreeLevel = "Master's Degree",
                intakeSeason = "Winter 2026/27",
                country = "Germany",
                status = ApplicationStatus.ACTION_REQUIRED,
                submittedDate = "05 February 2026",
                nextMilestone = "Upload Certified Copy of Language Proficiency Test Score",
                completionPercentage = 0.60f
            ),
            Application(
                applicationId = "app_3",
                userId = "student_101",
                universityId = "uni_3",
                universityName = "University of Toronto",
                universityLogoUrl = "https://images.unsplash.com/photo-1562774053-701939374585?w=300&q=80",
                programId = "p_3",
                programName = "Master of Applied Science in ECE",
                degreeLevel = "Master's Degree",
                intakeSeason = "Fall 2026",
                country = "Canada",
                status = ApplicationStatus.OFFER_RECEIVED,
                submittedDate = "10 November 2025",
                nextMilestone = "Accept Conditional Offer & Pay Tuition Deposit",
                completionPercentage = 1.0f
            )
        )
    )

    private val documentsState = MutableStateFlow<List<StudentDocument>>(
        listOf(
            StudentDocument("doc_1", "student_101", "International Passport", DocumentCategory.PASSPORT, "Passport_Scan_2026.pdf", "2.4 MB", "10 Jan 2026", "https://convoy.storage/docs/doc_1.pdf", true),
            StudentDocument("doc_2", "student_101", "Bachelor's Degree Transcript", DocumentCategory.TRANSCRIPT, "Official_Transcript_BSc.pdf", "4.1 MB", "12 Jan 2026", "https://convoy.storage/docs/doc_2.pdf", true),
            StudentDocument("doc_3", "student_101", "Statement of Purpose (SOP)", DocumentCategory.SOP, "SOP_Convoy_Oxford.docx", "512 KB", "14 Jan 2026", "https://convoy.storage/docs/doc_3.pdf", true),
            StudentDocument("doc_4", "student_101", "IELTS Academic Test Result", DocumentCategory.LANGUAGE_TEST, null, null, null, null, false)
        )
    )

    private val announcementsState = MutableStateFlow<List<Announcement>>(
        listOf(
            Announcement("ann_1", "Global Higher Education Fair 2026", "Join 50+ university admissions officers live this Saturday on Convoy.", "ALL_STUDENTS", "2026-02-10", true, "https://convoy.edu/events"),
            Announcement("ann_2", "UK Chevening Scholarship Deadline Approaching", "Final reminder for UK FCDO Chevening submissions.", "APPLICANTS", "2026-02-05", false, null)
        )
    )

    private val referralsState = MutableStateFlow<List<Referral>>(
        listOf(
            Referral(
                referralId = "ref_101",
                referrerUserId = "student_101",
                referrerName = "Alex Mercer",
                referralCode = "ALEX-REF101",
                referredUserId = "student_102",
                referredStudentName = "Elena Rostova",
                referredEmail = "elena.r@globalstudy.net",
                status = ReferralStatus.QUALIFIED,
                createdAt = "2026-01-20T10:30:00Z",
                rewardAmount = 100.0,
                rewardAmountFormatted = "$100 USD",
                paymentStatus = "Awaiting Admin Review",
                qualifyingApplicationId = "app_102",
                qualificationDetails = "Submitted application for Technical University of Munich (MSc Data Engineering)"
            ),
            Referral(
                referralId = "ref_102",
                referrerUserId = "student_101",
                referrerName = "Alex Mercer",
                referralCode = "ALEX-REF101",
                referredUserId = "student_103",
                referredStudentName = "Kaito Tanaka",
                referredEmail = "kaito.tanaka@tokyo.ac.jp",
                status = ReferralStatus.APPROVED,
                createdAt = "2026-02-04T14:15:00Z",
                rewardAmount = 100.0,
                rewardAmountFormatted = "$100 USD",
                paymentStatus = "Approved - Ready for Payout",
                qualifyingApplicationId = "app_103",
                qualificationDetails = "Submitted application for University of Toronto (BSc Computer Science)",
                adminNote = "Verified complete application submission."
            ),
            Referral(
                referralId = "ref_103",
                referrerUserId = "student_103",
                referrerName = "Kaito Tanaka",
                referralCode = "KAITO-REF103",
                referredUserId = "student_104",
                referredStudentName = "Priya Sharma",
                referredEmail = "priya.sharma@iit.ac.in",
                status = ReferralStatus.PAID,
                createdAt = "2026-02-07T09:00:00Z",
                rewardAmount = 100.0,
                rewardAmountFormatted = "$100 USD",
                paymentStatus = "Paid ($100 USD transferred)",
                qualifyingApplicationId = "app_104",
                qualificationDetails = "Submitted application for University of Melbourne",
                adminNote = "Payout processed via Wire Transfer on Feb 8, 2026."
            ),
            Referral(
                referralId = "ref_104",
                referrerUserId = "student_102",
                referrerName = "Elena Rostova",
                referralCode = "ELENA-REF102",
                referredEmail = "david.k@university.edu",
                status = ReferralStatus.PENDING,
                createdAt = "2026-02-08T11:00:00Z",
                rewardAmount = 100.0,
                rewardAmountFormatted = "$100 USD",
                paymentStatus = "Unpaid",
                qualificationDetails = "Referred user registered. Awaiting qualifying application submission."
            )
        )
    )

    private val studentsState = MutableStateFlow<List<User>>(
        listOf(
            User("student_101", "Alex Mercer", "alex.mercer@student.org", UserRole.STUDENT, "+1 (555) 382-9102", "United States"),
            User("student_102", "Elena Rostova", "elena.r@globalstudy.net", UserRole.STUDENT, "+44 20 7946 0912", "Germany"),
            User("student_103", "Kaito Tanaka", "kaito.tanaka@tokyo.ac.jp", UserRole.STUDENT, "+81 3 5555 0143", "Japan"),
            User("student_104", "Priya Sharma", "priya.sharma@iit.ac.in", UserRole.STUDENT, "+91 98765 43210", "India"),
            User("student_105", "Carlos Gomez", "carlos.gomez@bogota.edu.co", UserRole.STUDENT, "+57 1 2345678", "Colombia")
        )
    )

    private val recentActivitiesState = MutableStateFlow<List<RecentActivity>>(
        listOf(
            RecentActivity("act_1", "New Application Submitted", "Alex Mercer applied for MSc in Computer Science at University of Oxford", "10 mins ago", ActivityType.APPLICATION_SUBMITTED, "Alex Mercer"),
            RecentActivity("act_2", "Document Uploaded", "Elena Rostova uploaded Academic Transcripts", "45 mins ago", ActivityType.DOCUMENT_UPLOADED, "Elena Rostova"),
            RecentActivity("act_3", "Scholarship Updated", "Chevening UK Scholarship deadline updated to Nov 03, 2026", "2 hours ago", ActivityType.SCHOLARSHIP_UPDATED, "System Admin"),
            RecentActivity("act_4", "Student Registered", "Carlos Gomez created a student profile", "5 hours ago", ActivityType.STUDENT_REGISTERED, "Carlos Gomez"),
            RecentActivity("act_5", "Referral Converted", "Kaito Tanaka referred david.k@university.edu", "1 day ago", ActivityType.REFERRAL_CONVERTED, "Kaito Tanaka")
        )
    )

    override fun fetchUniversities(): Flow<List<University>> =
        universitiesState.map { list -> list.filter { it.status == EntityStatus.PUBLISHED } }

    override fun fetchAllUniversitiesForAdmin(): Flow<List<University>> = universitiesState

    override fun fetchUniversityById(id: String): Flow<University?> =
        universitiesState.map { list -> list.find { it.universityId == id } }

    override fun saveUniversity(university: University) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        universitiesState.update { list ->
            val index = list.indexOfFirst { it.universityId == university.universityId }
            if (index != -1) {
                list.toMutableList().apply { set(index, university) }
            } else {
                listOf(university) + list
            }
        }
    }

    override fun deleteUniversity(universityId: String) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        universitiesState.update { list ->
            list.filterNot { it.universityId == universityId }
        }
    }

    override fun updateUniversityStatus(universityId: String, status: EntityStatus) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        universitiesState.update { list ->
            list.map { uni ->
                if (uni.universityId == universityId) uni.copy(status = status) else uni
            }
        }
    }

    override fun fetchScholarships(): Flow<List<Scholarship>> =
        scholarshipsState.map { list -> list.filter { it.status == EntityStatus.PUBLISHED } }

    override fun fetchAllScholarshipsForAdmin(): Flow<List<Scholarship>> = scholarshipsState

    override fun fetchScholarshipById(id: String): Flow<Scholarship?> =
        scholarshipsState.map { list -> list.find { it.scholarshipId == id } }

    override fun saveScholarship(scholarship: Scholarship) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        scholarshipsState.update { list ->
            val index = list.indexOfFirst { it.scholarshipId == scholarship.scholarshipId }
            if (index != -1) {
                list.toMutableList().apply { set(index, scholarship) }
            } else {
                listOf(scholarship) + list
            }
        }
    }

    override fun deleteScholarship(scholarshipId: String) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        scholarshipsState.update { list ->
            list.filterNot { it.scholarshipId == scholarshipId }
        }
    }

    override fun updateScholarshipStatus(scholarshipId: String, status: EntityStatus) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        scholarshipsState.update { list ->
            list.map { sch ->
                if (sch.scholarshipId == scholarshipId) sch.copy(status = status) else sch
            }
        }
    }

    override fun fetchCountries(): Flow<List<Country>> =
        countriesState.map { list -> list.filter { it.status == EntityStatus.PUBLISHED } }

    override fun fetchAllCountriesForAdmin(): Flow<List<Country>> = countriesState

    override fun fetchCountryById(id: String): Flow<Country?> =
        countriesState.map { list -> list.find { it.countryId == id || it.name.equals(id, ignoreCase = true) } }

    override fun saveCountry(country: Country) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        countriesState.update { list ->
            val index = list.indexOfFirst { it.countryId == country.countryId }
            if (index != -1) {
                list.toMutableList().apply { set(index, country) }
            } else {
                listOf(country) + list
            }
        }
    }

    override fun deleteCountry(countryId: String) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        countriesState.update { list ->
            list.filterNot { it.countryId == countryId }
        }
    }

    override fun updateCountryStatus(countryId: String, status: EntityStatus) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        countriesState.update { list ->
            list.map { c ->
                if (c.countryId == countryId) c.copy(status = status) else c
            }
        }
    }

    override fun fetchPrograms(universityId: String?): Flow<List<Program>> =
        if (universityId == null) programsState
        else programsState.map { list -> list.filter { it.universityId == universityId } }

    override fun fetchApplications(userId: String): Flow<List<Application>> =
        applicationsState.map { list ->
            ConvoySecurityManager.filterPrivateApplications(list.filter { it.userId == userId })
        }

    override fun createApplication(application: Application) {
        if (!ConvoySecurityManager.canAccessApplication(application)) return
        val newHistory = application.statusHistory + StatusUpdate(
            status = application.status,
            timestamp = "Just now",
            note = "Draft application initiated"
        )
        applicationsState.update { listOf(application.copy(statusHistory = newHistory)) + it }
    }

    override fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String) {
        applicationsState.update { list ->
            list.map { app ->
                if (app.applicationId == applicationId) {
                    val newHistory = app.statusHistory + StatusUpdate(
                        status = status,
                        timestamp = "Just now",
                        note = note.ifBlank { "Status updated to ${status.label}" }
                    )
                    app.copy(
                        status = status,
                        updatedAt = "Just now",
                        statusHistory = newHistory
                    )
                } else app
            }
        }
    }

    override fun updateApplicationInternalNotes(applicationId: String, notes: String) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        applicationsState.update { list ->
            list.map { app ->
                if (app.applicationId == applicationId) {
                    app.copy(internalNotes = notes, updatedAt = "Just now")
                } else app
            }
        }
    }

    override fun requestMissingDocuments(applicationId: String, missingDocs: List<String>) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        applicationsState.update { list ->
            list.map { app ->
                if (app.applicationId == applicationId) {
                    val updatedRequested = (app.requestedDocuments + missingDocs).distinct()
                    val noteText = "Admin requested documents: ${missingDocs.joinToString(", ")}"
                    val newHistory = app.statusHistory + StatusUpdate(
                        status = ApplicationStatus.DOCUMENTS_REQUIRED,
                        timestamp = "Just now",
                        note = noteText
                    )
                    app.copy(
                        status = ApplicationStatus.DOCUMENTS_REQUIRED,
                        requestedDocuments = updatedRequested,
                        nextMilestone = "Upload required documents: ${missingDocs.joinToString(", ")}",
                        updatedAt = "Just now",
                        statusHistory = newHistory
                    )
                } else app
            }
        }
    }

    override fun submitApplication(applicationId: String) {
        var submittedApp: Application? = null
        applicationsState.update { list ->
            list.map { app ->
                if (app.applicationId == applicationId && ConvoySecurityManager.canAccessApplication(app)) {
                    val newHistory = app.statusHistory + StatusUpdate(
                        status = ApplicationStatus.SUBMITTED,
                        timestamp = "Just now",
                        note = "Application submitted by student for review"
                    )
                    val updated = app.copy(
                        status = ApplicationStatus.SUBMITTED,
                        submittedDate = "Just now",
                        nextMilestone = "Under preliminary review by Convoy admissions team",
                        completionPercentage = 0.80f,
                        updatedAt = "Just now",
                        statusHistory = newHistory
                    )
                    submittedApp = updated
                    updated
                } else app
            }
        }

        // Trigger referral qualification if applicable
        submittedApp?.let { app ->
            val studentId = app.userId
            val studentEmail = studentsState.value.find { it.userId == studentId }?.email ?: ""

            referralsState.update { refs ->
                // Check if student already has a qualified/approved/paid referral (prevent multiple rewards)
                val alreadyRewarding = refs.any { ref ->
                    (ref.referredUserId == studentId || (studentEmail.isNotBlank() && ref.referredEmail.equals(studentEmail, ignoreCase = true))) &&
                            (ref.status == ReferralStatus.QUALIFIED || ref.status == ReferralStatus.APPROVED || ref.status == ReferralStatus.PAID)
                }

                if (alreadyRewarding) {
                    refs
                } else {
                    refs.map { ref ->
                        val isMatchingStudent = (ref.referredUserId == studentId) ||
                                (studentEmail.isNotBlank() && ref.referredEmail.equals(studentEmail, ignoreCase = true))
                        if (isMatchingStudent && ref.status == ReferralStatus.PENDING) {
                            ref.copy(
                                status = ReferralStatus.QUALIFIED,
                                referredUserId = studentId,
                                qualifyingApplicationId = app.applicationId,
                                qualificationDetails = "Submitted qualifying application for ${app.universityName} (${app.programName})",
                                paymentStatus = "Awaiting Admin Review"
                            )
                        } else ref
                    }
                }
            }
        }
    }

    override fun getUserReferralCode(userId: String): String {
        val student = studentsState.value.find { it.userId == userId }
        val namePart = (student?.name ?: "CONVOY").replace(" ", "").uppercase().take(5)
        val idPart = userId.takeLast(3).uppercase().ifBlank { "101" }
        return "$namePart-REF$idPart"
    }

    override fun applyReferralCode(
        code: String,
        referredUserId: String,
        referredName: String,
        referredEmail: String
    ): Pair<Boolean, String> {
        val trimmedCode = code.trim().uppercase()
        val trimmedEmail = referredEmail.trim().lowercase()

        if (trimmedCode.isBlank()) {
            return Pair(false, "Referral code cannot be blank.")
        }
        if (trimmedEmail.isBlank()) {
            return Pair(false, "Referred student email cannot be blank.")
        }

        // Find referrer matching code or student list
        val allUsers = studentsState.value
        val referrer = allUsers.find { getUserReferralCode(it.userId) == trimmedCode }
            ?: allUsers.find { it.userId.equals(trimmedCode, ignoreCase = true) }

        if (referrer == null) {
            return Pair(false, "Invalid referral code entered. Please check the code and try again.")
        }

        // Prevention Rule 1: Self-Referral Prevention
        if (referrer.userId == referredUserId || referrer.email.equals(trimmedEmail, ignoreCase = true)) {
            return Pair(false, "Self-referral detected. You cannot use your own referral code under Convoy Referral Policy.")
        }

        // Prevention Rule 2: Duplicate Referral Prevention
        val existingRefs = referralsState.value
        val isDuplicate = existingRefs.any {
            it.referredEmail.equals(trimmedEmail, ignoreCase = true) || (it.referredUserId != null && it.referredUserId == referredUserId)
        }
        if (isDuplicate) {
            return Pair(false, "Duplicate referral detected. This student email/account has already been referred on Convoy.")
        }

        // Create Referral Record
        val newRef = Referral(
            referralId = "ref_${UUID.randomUUID().toString().take(6)}",
            referrerUserId = referrer.userId,
            referrerName = referrer.name,
            referralCode = trimmedCode,
            referredUserId = referredUserId,
            referredStudentName = referredName.ifBlank { trimmedEmail.substringBefore("@") },
            referredEmail = trimmedEmail,
            status = ReferralStatus.PENDING,
            createdAt = "Just now",
            rewardAmount = 100.0,
            rewardAmountFormatted = "$100 USD",
            paymentStatus = "Unpaid",
            qualificationDetails = "Referred student registered. Awaiting qualifying application submission."
        )

        referralsState.update { listOf(newRef) + it }
        return Pair(true, "Referral code applied successfully! $100 reward will qualify when you submit a university application.")
    }

    override fun updateReferralStatus(referralId: String, newStatus: ReferralStatus, adminNote: String) {
        if (!ConvoySecurityManager.canManageAdminContent()) return

        referralsState.update { list ->
            list.map { ref ->
                if (ref.referralId == referralId) {
                    val pStatus = when (newStatus) {
                        ReferralStatus.PENDING -> "Unpaid"
                        ReferralStatus.QUALIFIED -> "Awaiting Admin Review"
                        ReferralStatus.APPROVED -> "Approved - Ready for Payout"
                        ReferralStatus.PAID -> "Paid ($100 USD transferred)"
                        ReferralStatus.REJECTED -> "Rejected"
                    }
                    ref.copy(
                        status = newStatus,
                        paymentStatus = pStatus,
                        adminNote = adminNote.ifBlank { ref.adminNote }
                    )
                } else ref
            }
        }
    }

    override fun saveDraftApplication(application: Application) {
        applicationsState.update { list ->
            val index = list.indexOfFirst { it.applicationId == application.applicationId }
            if (index != -1) {
                list.toMutableList().apply { set(index, application.copy(updatedAt = "Just now")) }
            } else {
                listOf(application) + list
            }
        }
    }

    override fun withdrawApplication(applicationId: String) {
        applicationsState.update { list ->
            list.map { app ->
                if (app.applicationId == applicationId && ConvoySecurityManager.canAccessApplication(app)) {
                    val newHistory = app.statusHistory + StatusUpdate(
                        status = ApplicationStatus.WITHDRAWN,
                        timestamp = "Just now",
                        note = "Application withdrawn by student"
                    )
                    app.copy(
                        status = ApplicationStatus.WITHDRAWN,
                        updatedAt = "Just now",
                        statusHistory = newHistory
                    )
                } else app
            }
        }
    }

    override fun fetchDocuments(userId: String): Flow<List<StudentDocument>> =
        documentsState.map { list ->
            ConvoySecurityManager.filterPrivateDocuments(list.filter { it.userId == userId })
        }

    override fun addDocument(document: StudentDocument) {
        if (!ConvoySecurityManager.canAccessDocument(document)) return
        documentsState.update { list ->
            val index = list.indexOfFirst { it.category == document.category && !it.isUploaded }
            if (index != -1) {
                list.toMutableList().apply { set(index, document) }
            } else {
                listOf(document) + list
            }
        }
    }

    override fun deleteDocument(documentId: String) {
        documentsState.update { list ->
            list.filterNot { doc -> doc.documentId == documentId && ConvoySecurityManager.canAccessDocument(doc) }
        }
    }

    override fun fetchAnnouncements(): Flow<List<Announcement>> = announcementsState

    override fun fetchReferrals(userId: String): Flow<List<Referral>> =
        referralsState.map { list -> list.filter { it.referrerUserId == userId } }

    private val leadsState = MutableStateFlow<List<Lead>>(
        listOf(
            Lead(
                leadId = "lead_101",
                studentUserId = "usr_student_1",
                studentName = "Alex Rivera",
                studentEmail = "alex.rivera@student.edu",
                studentPhone = "+1 (555) 382-9102",
                country = "United Kingdom",
                universityId = "uni_1",
                universityName = "University of Oxford",
                source = "Request Information",
                date = "2026-02-08",
                status = LeadStatus.NEW,
                notes = "Interested in MSc Computer Science intake Fall 2026."
            ),
            Lead(
                leadId = "lead_102",
                studentUserId = "usr_student_1",
                studentName = "Alex Rivera",
                studentEmail = "alex.rivera@student.edu",
                studentPhone = "+1 (555) 382-9102",
                country = "Germany",
                universityId = "uni_2",
                universityName = "Technical University of Munich (TUM)",
                source = "Save University",
                date = "2026-02-07",
                status = LeadStatus.CONTACTED,
                notes = "Saved university to profile."
            )
        )
    )

    override fun fetchLeads(): Flow<List<Lead>> = leadsState

    override fun createLead(lead: Lead): Pair<Boolean, String> {
        val currentLeads = leadsState.value
        val isDuplicate = currentLeads.any { existing ->
            (existing.studentUserId == lead.studentUserId || (lead.studentEmail.isNotBlank() && existing.studentEmail.equals(lead.studentEmail, ignoreCase = true))) &&
                    existing.source == lead.source &&
                    ((lead.universityId != null && existing.universityId == lead.universityId) ||
                     (lead.scholarshipId != null && existing.scholarshipId == lead.scholarshipId))
        }

        if (isDuplicate) {
            return Pair(false, "You have already submitted an inquiry for this ${if (lead.universityId != null) "university" else "scholarship"}. An advisor is processing your request.")
        }

        val newLead = lead.copy(
            leadId = if (lead.leadId.isBlank()) "lead_${System.currentTimeMillis().toString().takeLast(6)}" else lead.leadId,
            date = if (lead.date.isBlank()) "2026-02-09" else lead.date
        )

        leadsState.update { listOf(newLead) + it }

        recentActivitiesState.update {
            listOf(
                RecentActivity(
                    activityId = "act_${System.currentTimeMillis().toString().takeLast(6)}",
                    title = "New Lead Generated",
                    description = "${lead.studentName} generated lead via ${lead.source} for ${lead.universityName ?: lead.scholarshipName ?: "Convoy"}",
                    timestamp = "Just now",
                    type = ActivityType.STUDENT_REGISTERED,
                    actorName = lead.studentName
                )
            ) + it
        }

        return Pair(true, "Request successfully submitted! An advisor will reach out to you shortly.")
    }

    override fun updateLeadStatus(leadId: String, status: LeadStatus, notes: String) {
        leadsState.update { list ->
            list.map { item ->
                if (item.leadId == leadId) {
                    item.copy(
                        status = status,
                        notes = if (notes.isNotBlank()) notes else item.notes
                    )
                } else item
            }
        }
    }

    private val requirementsState = MutableStateFlow<List<UniversityRequirement>>(
        listOf(
            UniversityRequirement(
                requirementId = "req_101",
                universityId = "All",
                universityName = "All Universities",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.PASSPORT,
                title = "International Passport",
                isRequired = true,
                minScore = "",
                instructions = "Valid international passport bio page copy with at least 6 months validity.",
                isPublished = true
            ),
            UniversityRequirement(
                requirementId = "req_102",
                universityId = "All",
                universityName = "All Universities",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.ACADEMIC_TRANSCRIPT,
                title = "Official Academic Transcripts",
                isRequired = true,
                minScore = "",
                instructions = "Complete official academic transcripts of all secondary/higher education coursework.",
                isPublished = true
            ),
            UniversityRequirement(
                requirementId = "req_103",
                universityId = "All",
                universityName = "All Universities",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.ACADEMIC_CERTIFICATE,
                title = "Degree / Higher Secondary Certificate",
                isRequired = true,
                minScore = "",
                instructions = "Official degree or graduation completion certificate.",
                isPublished = true
            ),
            UniversityRequirement(
                requirementId = "req_104",
                universityId = "uni_1",
                universityName = "University of Oxford",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.IELTS,
                title = "IELTS Academic Test Result",
                isRequired = false,
                minScore = "7.5 Overall",
                instructions = "Minimum overall band score of 7.5 with no individual band below 7.0.",
                isPublished = true
            ),
            UniversityRequirement(
                requirementId = "req_105",
                universityId = "uni_1",
                universityName = "University of Oxford",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.SOP,
                title = "Statement of Purpose (SOP)",
                isRequired = true,
                minScore = "",
                instructions = "500-1000 word personal statement outlining academic goals and research interests.",
                isPublished = true
            ),
            UniversityRequirement(
                requirementId = "req_106",
                universityId = "uni_2",
                universityName = "Technical University of Munich (TUM)",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.IELTS,
                title = "IELTS / English Proficiency",
                isRequired = true,
                minScore = "6.5 Overall",
                instructions = "Minimum overall score 6.5 or official English medium of instruction (MOI) certificate.",
                isPublished = true
            ),
            UniversityRequirement(
                requirementId = "req_107",
                universityId = "uni_2",
                universityName = "Technical University of Munich (TUM)",
                programName = "All Programs",
                intakeSeason = "All Intakes",
                type = RequirementType.BANK_STATEMENT,
                title = "German Blocked Account / Financial Proof",
                isRequired = true,
                minScore = "€11,208 / year",
                instructions = "Official blocked bank account or sponsorship statement meeting German embassy standards.",
                isPublished = true
            )
        )
    )

    override fun fetchRequirements(
        universityId: String?,
        programName: String?,
        intakeSeason: String?
    ): Flow<List<UniversityRequirement>> {
        return requirementsState.map { list ->
            list.filter { req ->
                req.isPublished &&
                        (universityId == null || req.universityId == "All" || req.universityId == universityId) &&
                        (programName == null || req.programName == "All Programs" || req.programName == programName) &&
                        (intakeSeason == null || req.intakeSeason == "All Intakes" || req.intakeSeason == intakeSeason)
            }
        }
    }

    override fun fetchAllRequirementsForAdmin(): Flow<List<UniversityRequirement>> = requirementsState

    override fun saveRequirement(requirement: UniversityRequirement) {
        val reqId = if (requirement.requirementId.isBlank()) "req_${System.currentTimeMillis().toString().takeLast(6)}" else requirement.requirementId
        val finalReq = requirement.copy(requirementId = reqId)
        requirementsState.update { list ->
            val index = list.indexOfFirst { it.requirementId == finalReq.requirementId }
            if (index >= 0) {
                list.toMutableList().apply { set(index, finalReq) }
            } else {
                listOf(finalReq) + list
            }
        }
    }

    override fun deleteRequirement(requirementId: String) {
        requirementsState.update { list -> list.filter { it.requirementId != requirementId } }
    }

    override fun updateRequirementStatus(requirementId: String, isPublished: Boolean) {
        requirementsState.update { list ->
            list.map { req ->
                if (req.requirementId == requirementId) req.copy(isPublished = isPublished) else req
            }
        }
    }

    private val assistanceRequestsState = MutableStateFlow<List<AssistanceRequest>>(
        listOf(
            AssistanceRequest(
                requestId = "req_ast_001",
                userId = "std_1",
                studentName = "Alex Rivera",
                studentEmail = "alex.rivera@example.com",
                studentPhone = "+1 555-0192",
                serviceType = AssistanceType.APPLICATION_GUIDANCE,
                targetUniversityName = "University of Oxford",
                targetProgramName = "MSc Computer Science",
                studentNotes = "Need assistance with UK student visa financial requirements and transcript attestation.",
                status = AssistanceStatus.IN_PROGRESS,
                assignedCounselor = "Sarah Jenkins (Senior UK Counselor)",
                internalNotes = "Verified initial transcripts. Guidance note sent on tier 4 visa financial proof rules.",
                guidanceMessages = listOf(
                    GuidanceMessage(
                        id = "msg_1",
                        senderName = "Alex Rivera",
                        isFromAdmin = false,
                        message = "Hi! I submitted my Oxford application and need help with document verification.",
                        timestamp = System.currentTimeMillis() - 86400000 * 2
                    ),
                    GuidanceMessage(
                        id = "msg_2",
                        senderName = "Sarah Jenkins",
                        isFromAdmin = true,
                        message = "Hello Alex! I have reviewed your documents. Please ensure your bank statement holds the required maintenance funds for at least 28 consecutive days.",
                        timestamp = System.currentTimeMillis() - 86400000
                    )
                )
            ),
            AssistanceRequest(
                requestId = "req_ast_002",
                userId = "std_2",
                studentName = "Sophia Chen",
                studentEmail = "sophia.chen@example.com",
                studentPhone = "+1 555-0184",
                serviceType = AssistanceType.UNIVERSITY_SELECTION,
                targetUniversityName = "Technical University of Munich (TUM)",
                targetProgramName = "MSc Data Engineering",
                studentNotes = "Looking for target and safety universities in Germany offering English-taught master programs.",
                status = AssistanceStatus.UNDER_REVIEW,
                assignedCounselor = "Unassigned",
                internalNotes = "New request received from student dashboard."
            )
        )
    )

    override fun fetchAssistanceRequests(userId: String?): Flow<List<AssistanceRequest>> {
        return assistanceRequestsState.map { list ->
            if (userId == null) list else list.filter { it.userId == userId || userId == "All" }
        }
    }

    override fun fetchAllAssistanceRequestsForAdmin(): Flow<List<AssistanceRequest>> = assistanceRequestsState

    override fun createAssistanceRequest(request: AssistanceRequest) {
        val reqId = if (request.requestId.isBlank()) "req_ast_${System.currentTimeMillis().toString().takeLast(6)}" else request.requestId
        val finalReq = request.copy(requestId = reqId, requestedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        assistanceRequestsState.update { listOf(finalReq) + it }
    }

    override fun updateAssistanceStatus(
        requestId: String,
        status: AssistanceStatus,
        counselor: String,
        internalNotes: String
    ) {
        assistanceRequestsState.update { list ->
            list.map { req ->
                if (req.requestId == requestId) {
                    req.copy(
                        status = status,
                        assignedCounselor = counselor.ifBlank { req.assignedCounselor },
                        internalNotes = internalNotes.ifBlank { req.internalNotes },
                        updatedAt = System.currentTimeMillis()
                    )
                } else req
            }
        }
    }

    override fun addGuidanceMessage(requestId: String, message: GuidanceMessage) {
        assistanceRequestsState.update { list ->
            list.map { req ->
                if (req.requestId == requestId) {
                    req.copy(
                        guidanceMessages = req.guidanceMessages + message,
                        updatedAt = System.currentTimeMillis()
                    )
                } else req
            }
        }
    }

    private val partnersState = MutableStateFlow<List<Partner>>(
        listOf(
            Partner(
                partnerId = "partner_1",
                name = "University of Melbourne",
                type = PartnerType.UNIVERSITY,
                country = "Australia",
                contactInfo = "admissions-partners@unimelb.edu.au | +61 3 9035 5511",
                website = "https://unimelb.edu.au",
                partnershipStatus = PartnershipStatus.ACTIVE,
                agreementStatus = "Signed Direct Contract (Valid thru 2028)",
                commissionInfo = "15% of Year 1 tuition fee upon Census date verification",
                notes = "Verified direct university partner with dedicated admissions portal.",
                createdDate = "2025-09-15",
                lastUpdated = "2026-01-10"
            ),
            Partner(
                partnerId = "partner_2",
                name = "Global Academic Placement Network",
                type = PartnerType.RECRUITMENT_PARTNER,
                country = "United Kingdom",
                contactInfo = "partnerships@gapn-edu.co.uk",
                website = "https://gapn-edu.co.uk",
                partnershipStatus = PartnershipStatus.NEGOTIATING,
                agreementStatus = "MOU Draft under Legal Review",
                commissionInfo = "Tiered 8-12% sub-agent split per enrolled student",
                notes = "Sub-agent aggregator covering UK mid-tier universities.",
                createdDate = "2026-01-20",
                lastUpdated = "2026-02-05"
            ),
            Partner(
                partnerId = "partner_3",
                name = "StudyUSA EduServices",
                type = PartnerType.AUTHORIZED_EDUCATION_PROVIDER,
                country = "United States",
                contactInfo = "info@studyusa-provider.org",
                website = "https://studyusa-provider.org",
                partnershipStatus = PartnershipStatus.ACTIVE,
                agreementStatus = "Signed Articulation Agreement",
                commissionInfo = "Flat $2,000 USD per matriculated undergraduate",
                notes = "Pathway provider for US state university transfer programs.",
                createdDate = "2025-11-01",
                lastUpdated = "2026-02-01"
            ),
            Partner(
                partnerId = "partner_4",
                name = "Toronto Global Education Hub",
                type = PartnerType.OTHER_VERIFIED_PARTNER,
                country = "Canada",
                contactInfo = "admin@torontoglobaledu.ca",
                website = "https://torontoglobaledu.ca",
                partnershipStatus = PartnershipStatus.PROSPECT,
                agreementStatus = "Initial Exploratory Talks",
                commissionInfo = "Proposed 10% enrollment bonus",
                notes = "Canada college pathway network.",
                createdDate = "2026-02-02",
                lastUpdated = "2026-02-08"
            )
        )
    )

    override fun fetchPartners(): Flow<List<Partner>> =
        if (ConvoySecurityManager.canManageAdminContent()) partnersState
        else kotlinx.coroutines.flow.flowOf(emptyList())

    override fun savePartner(partner: Partner) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        val pId = if (partner.partnerId.isBlank()) "partner_${UUID.randomUUID().toString().take(6)}" else partner.partnerId
        val finalP = partner.copy(partnerId = pId, lastUpdated = "2026-02-09")
        partnersState.update { list ->
            val index = list.indexOfFirst { it.partnerId == pId }
            if (index >= 0) {
                list.toMutableList().apply { set(index, finalP) }
            } else {
                listOf(finalP) + list
            }
        }
    }

    override fun deletePartner(partnerId: String) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        partnersState.update { list -> list.filterNot { it.partnerId == partnerId } }
    }

    override fun updatePartnerStatus(partnerId: String, status: PartnershipStatus) {
        if (!ConvoySecurityManager.canManageAdminContent()) return
        partnersState.update { list ->
            list.map { p ->
                if (p.partnerId == partnerId) {
                    p.copy(partnershipStatus = status, lastUpdated = "2026-02-09")
                } else p
            }
        }
    }

    override fun updateApplicationAttribution(
        applicationId: String,
        partnerId: String?,
        partnerName: String?,
        source: String,
        commissionEligible: Boolean,
        commissionStatus: CommissionStatus,
        commissionAmount: String?
    ) {
        applicationsState.update { list ->
            list.map { app ->
                if (app.applicationId == applicationId) {
                    app.copy(
                        partnerId = partnerId,
                        partnerName = partnerName,
                        applicationSource = source.ifBlank { app.applicationSource },
                        commissionEligible = commissionEligible,
                        commissionStatus = commissionStatus,
                        commissionAmount = commissionAmount
                    )
                } else app
            }
        }
    }

    private val sponsoredListingsState = MutableStateFlow<List<SponsoredListing>>(
        listOf(
            SponsoredListing(
                listingId = "sp_001",
                entityType = ListingEntityType.UNIVERSITY,
                entityId = "uni_1",
                entityName = "University of Melbourne",
                listingType = ListingType.FEATURED,
                startDate = "2026-01-01",
                endDate = "2026-12-31",
                placement = "Search & Discovery Top",
                status = ListingStatus.ACTIVE,
                sponsorPartner = "University of Melbourne Admissions",
                internalNotes = "Verified direct partner campaign - 2026 Academic Year",
                createdDate = "2026-01-05"
            ),
            SponsoredListing(
                listingId = "sp_002",
                entityType = ListingEntityType.SCHOLARSHIP,
                entityId = "sch_1",
                entityName = "Chevening UK Government Scholarship",
                listingType = ListingType.SPONSORED,
                startDate = "2026-02-01",
                endDate = "2026-11-30",
                placement = "Scholarship Spotlight Card",
                status = ListingStatus.ACTIVE,
                sponsorPartner = "UK Foreign, Commonwealth & Development Office",
                internalNotes = "Global official promotion for 2026 intake",
                createdDate = "2026-01-20"
            ),
            SponsoredListing(
                listingId = "sp_003",
                entityType = ListingEntityType.PROGRAM,
                entityId = "prog_001",
                entityName = "MSc Data Science & AI",
                listingType = ListingType.SPONSORED,
                startDate = "2026-09-01",
                endDate = "2026-12-31",
                placement = "Program Explorer Featured Banner",
                status = ListingStatus.SCHEDULED,
                sponsorPartner = "Imperial College London",
                internalNotes = "Scheduled Fall 2026 recruitment campaign",
                createdDate = "2026-02-05"
            ),
            SponsoredListing(
                listingId = "sp_004",
                entityType = ListingEntityType.UNIVERSITY,
                entityId = "uni_old",
                entityName = "Old College Network",
                listingType = ListingType.FEATURED,
                startDate = "2025-01-01",
                endDate = "2025-12-31",
                placement = "Home Page Hero",
                status = ListingStatus.EXPIRED,
                sponsorPartner = "Legacy Ad Network",
                internalNotes = "Expired contract - automatically hidden from user listings",
                createdDate = "2025-01-01"
            )
        )
    )

    override fun fetchSponsoredListings(): Flow<List<SponsoredListing>> = sponsoredListingsState

    override fun saveSponsoredListing(listing: SponsoredListing) {
        val id = if (listing.listingId.isBlank()) "sp_${UUID.randomUUID().toString().take(6)}" else listing.listingId
        val finalListing = listing.copy(listingId = id)
        sponsoredListingsState.update { list ->
            val index = list.indexOfFirst { it.listingId == id }
            if (index >= 0) {
                list.toMutableList().apply { set(index, finalListing) }
            } else {
                listOf(finalListing) + list
            }
        }
    }

    override fun deleteSponsoredListing(listingId: String) {
        sponsoredListingsState.update { list -> list.filterNot { it.listingId == listingId } }
    }

    override fun updateSponsoredListingStatus(listingId: String, status: ListingStatus) {
        sponsoredListingsState.update { list ->
            list.map { sp ->
                if (sp.listingId == listingId) sp.copy(status = status) else sp
            }
        }
    }

    private val analyticsEventsState = MutableStateFlow<List<AnalyticsEvent>>(
        listOf(
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_1", targetName = "University of Oxford", country = "United Kingdom", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_1", targetName = "University of Oxford", country = "United Kingdom", timestamp = System.currentTimeMillis() - 86400000L * 1),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_2", targetName = "Technical University of Munich", country = "Germany", timestamp = System.currentTimeMillis() - 86400000L * 3),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_3", targetName = "University of Copenhagen", country = "Denmark", timestamp = System.currentTimeMillis() - 86400000L * 4),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_4", targetName = "University of Malaya", country = "Malaysia", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_5", targetName = "University of Cyprus", country = "Cyprus", timestamp = System.currentTimeMillis() - 86400000L * 1),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_6", targetName = "Harvard University", country = "United States", timestamp = System.currentTimeMillis() - 86400000L * 5),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_7", targetName = "University of Melbourne", country = "Australia", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_VIEW, targetId = "uni_8", targetName = "University of Barcelona", country = "Spain", timestamp = System.currentTimeMillis() - 86400000L * 3),
            
            AnalyticsEvent(eventType = AnalyticsEventType.SCHOLARSHIP_VIEW, targetId = "sch_1", targetName = "Oxford Clarendon Fund", country = "United Kingdom", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.SCHOLARSHIP_VIEW, targetId = "sch_2", targetName = "DAAD Master Scholarship", country = "Germany", timestamp = System.currentTimeMillis() - 86400000L * 3),
            AnalyticsEvent(eventType = AnalyticsEventType.SCHOLARSHIP_VIEW, targetId = "sch_3", targetName = "Danish Government Scholarship", country = "Denmark", timestamp = System.currentTimeMillis() - 86400000L * 1),
            AnalyticsEvent(eventType = AnalyticsEventType.SCHOLARSHIP_VIEW, targetId = "sch_4", targetName = "Malaysian International Scholarship", country = "Malaysia", timestamp = System.currentTimeMillis() - 86400000L * 4),
            AnalyticsEvent(eventType = AnalyticsEventType.SCHOLARSHIP_VIEW, targetId = "sch_5", targetName = "Cyprus State Merit Grant", country = "Cyprus", timestamp = System.currentTimeMillis() - 86400000L * 2),

            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_uk", targetName = "United Kingdom", country = "United Kingdom", timestamp = System.currentTimeMillis() - 86400000L * 1),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_de", targetName = "Germany", country = "Germany", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_dk", targetName = "Denmark", country = "Denmark", timestamp = System.currentTimeMillis() - 86400000L * 3),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_my", targetName = "Malaysia", country = "Malaysia", timestamp = System.currentTimeMillis() - 86400000L * 1),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_cy", targetName = "Cyprus", country = "Cyprus", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_us", targetName = "United States", country = "United States", timestamp = System.currentTimeMillis() - 86400000L * 4),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_au", targetName = "Australia", country = "Australia", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.COUNTRY_VIEW, targetId = "cnt_es", targetName = "Spain", country = "Spain", timestamp = System.currentTimeMillis() - 86400000L * 3),

            AnalyticsEvent(eventType = AnalyticsEventType.SEARCH_PERFORMED, searchQuery = "Computer Science Scholarships", timestamp = System.currentTimeMillis() - 3600000L * 4),
            AnalyticsEvent(eventType = AnalyticsEventType.SEARCH_PERFORMED, searchQuery = "Master Programs in Denmark", timestamp = System.currentTimeMillis() - 3600000L * 8),
            AnalyticsEvent(eventType = AnalyticsEventType.SEARCH_PERFORMED, searchQuery = "Tuition-free Germany", timestamp = System.currentTimeMillis() - 86400000L * 1),
            AnalyticsEvent(eventType = AnalyticsEventType.SEARCH_PERFORMED, searchQuery = "Malaysia University Intake", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.SEARCH_PERFORMED, searchQuery = "Cyprus Medical Degree", timestamp = System.currentTimeMillis() - 86400000L * 3),

            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_SAVED, targetId = "uni_1", targetName = "University of Oxford", country = "United Kingdom", timestamp = System.currentTimeMillis() - 86400000L * 2),
            AnalyticsEvent(eventType = AnalyticsEventType.UNIVERSITY_SAVED, targetId = "uni_2", targetName = "Technical University of Munich", country = "Germany", timestamp = System.currentTimeMillis() - 86400000L * 3)
        )
    )

    override fun trackAnalyticsEvent(event: AnalyticsEvent) {
        analyticsEventsState.update { list -> listOf(event) + list }
    }

    override fun fetchAnalyticsEvents(): Flow<List<AnalyticsEvent>> = analyticsEventsState

    override fun fetchAllApplicationsForAdmin(): Flow<List<Application>> = applicationsState

    override fun fetchAllDocumentsForAdmin(): Flow<List<StudentDocument>> = documentsState

    override fun fetchStudents(): Flow<List<User>> = studentsState

    override fun fetchAllReferralsForAdmin(): Flow<List<Referral>> = referralsState

    override fun fetchRecentActivities(): Flow<List<RecentActivity>> = recentActivitiesState

    // Support State & Implementation
    private val supportConfigState = MutableStateFlow(
        SupportConfig(
            supportEmail = "support@convoy.edu",
            phoneNumber = "+1 (800) 555-CONVOY",
            whatsappNumber = "+1 (800) 555-2668",
            officeHours = "Monday – Friday, 9:00 AM – 6:00 PM EST",
            linkedinUrl = "https://linkedin.com/company/convoy-edu",
            twitterUrl = "https://x.com/convoy_edu",
            instagramUrl = "https://instagram.com/convoy_edu",
            lastUpdatedBy = "System Administrator",
            updatedAt = System.currentTimeMillis()
        )
    )

    private val supportRequestsState = MutableStateFlow<List<SupportRequest>>(
        listOf(
            SupportRequest(
                requestId = "req_sup_101",
                userId = "user_001",
                studentName = "Alex Johnson",
                studentEmail = "alex.j@example.com",
                category = SupportCategory.APPLICATION,
                subject = "Clarification on Oxford Intake Deadline & Requirements",
                message = "Hi Convoy Support, I have started a draft application for MSc Computer Science at University of Oxford. Could you confirm if official GRE scores are mandatory for international applicants?",
                relatedApplicationId = "app_001",
                relatedUniversity = "University of Oxford",
                status = SupportStatus.IN_PROGRESS,
                assignedStaff = "Sarah Counselor",
                internalNotes = "Student verified. Checked Oxford entry guidelines for 2026 intake.",
                createdAt = System.currentTimeMillis() - 86400000L * 2,
                updatedAt = System.currentTimeMillis() - 86400000L * 1,
                replies = listOf(
                    SupportReply(
                        replyId = "rep_101_1",
                        senderId = "admin_001",
                        senderName = "Convoy Support (Sarah)",
                        isAdmin = true,
                        message = "Hello Alex! GRE scores are optional for Oxford MSc CS but strongly recommended if available. Your academic transcripts and SOP look great so far. Let us know if you need assistance with document verification!",
                        timestamp = System.currentTimeMillis() - 86400000L * 1
                    )
                )
            ),
            SupportRequest(
                requestId = "req_sup_102",
                userId = "user_001",
                studentName = "Alex Johnson",
                studentEmail = "alex.j@example.com",
                category = SupportCategory.DOCUMENT,
                subject = "Transcript Notarization Guidance",
                message = "I am uploading my undergraduate academic transcript. Do I need to get it notarized or certified by an official authority before submitting?",
                relatedDocumentContext = "Academic Transcript (B.Sc Computer Science)",
                status = SupportStatus.NEW,
                assignedStaff = "Unassigned",
                createdAt = System.currentTimeMillis() - 3600000L * 5,
                updatedAt = System.currentTimeMillis() - 3600000L * 5,
                replies = emptyList()
            )
        )
    )

    override fun fetchSupportRequests(userId: String?): Flow<List<SupportRequest>> {
        return if (userId.isNullOrBlank()) {
            supportRequestsState
        } else {
            supportRequestsState.map { list -> list.filter { it.userId == userId } }
        }
    }

    override fun fetchAllSupportRequestsForAdmin(): Flow<List<SupportRequest>> = supportRequestsState

    override fun createSupportRequest(request: SupportRequest): Pair<Boolean, String> {
        val newReq = request.copy(
            requestId = if (request.requestId.isBlank()) "req_sup_${UUID.randomUUID().toString().take(6)}" else request.requestId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        supportRequestsState.update { listOf(newReq) + it }
        return Pair(true, "Support request submitted successfully. Request ID: ${newReq.requestId}")
    }

    override fun addSupportReply(requestId: String, reply: SupportReply, newStatus: SupportStatus?) {
        supportRequestsState.update { list ->
            list.map { req ->
                if (req.requestId == requestId) {
                    val updatedReplies = req.replies + reply.copy(
                        replyId = if (reply.replyId.isBlank()) "rep_${UUID.randomUUID().toString().take(6)}" else reply.replyId,
                        timestamp = System.currentTimeMillis()
                    )
                    req.copy(
                        replies = updatedReplies,
                        status = newStatus ?: if (reply.isAdmin) SupportStatus.WAITING_FOR_STUDENT else SupportStatus.OPEN,
                        updatedAt = System.currentTimeMillis()
                    )
                } else req
            }
        }
    }

    override fun updateSupportStatus(
        requestId: String,
        status: SupportStatus,
        internalNotes: String,
        assignedStaff: String
    ) {
        supportRequestsState.update { list ->
            list.map { req ->
                if (req.requestId == requestId) {
                    req.copy(
                        status = status,
                        internalNotes = if (internalNotes.isNotBlank()) internalNotes else req.internalNotes,
                        assignedStaff = if (assignedStaff.isNotBlank()) assignedStaff else req.assignedStaff,
                        updatedAt = System.currentTimeMillis()
                    )
                } else req
            }
        }
    }

    override fun fetchSupportConfig(): Flow<SupportConfig> = supportConfigState

    override fun updateSupportConfig(config: SupportConfig) {
        supportConfigState.update {
            config.copy(updatedAt = System.currentTimeMillis())
        }
    }

    // -----------------------------------------------------------------
    // CHAT & COUNSELLOR HUB REAL-TIME STATE & IMPLEMENTATION
    // -----------------------------------------------------------------
    private val conversationsState = MutableStateFlow<List<ChatConversation>>(
        listOf(
            ChatConversation(
                conversationId = "conv_counsellor_1",
                studentId = "user_1",
                studentName = "Alex Rivera",
                studentEmail = "alex.rivera@example.com",
                conversationType = ConversationType.COUNSELLOR,
                title = "Elena Vance",
                subTitle = "Senior Study Abroad Counsellor",
                counsellorId = "counsellor_elena",
                counsellorName = "Elena Vance",
                counsellorAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
                counsellorRole = "Senior Study Abroad Counsellor",
                isCounsellorOnline = true,
                lastMessageText = "Hi! Welcome to Convoy. I'm here to help you with universities, scholarships, applications, and your study-abroad journey. How can I help you today?",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                unreadCountStudent = 1
            ),
            ChatConversation(
                conversationId = "conv_support_1",
                studentId = "user_1",
                studentName = "Alex Rivera",
                studentEmail = "alex.rivera@example.com",
                conversationType = ConversationType.SUPPORT,
                title = "Convoy Support Desk",
                subTitle = "24/7 Academic & System Support",
                counsellorId = "staff_support",
                counsellorName = "Convoy Support Team",
                counsellorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80",
                counsellorRole = "Support Desk Lead",
                isCounsellorOnline = true,
                lastMessageText = "Welcome to Convoy Support! Feel free to ask about application issues, document requirements, or portal technical support.",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                unreadCountStudent = 0
            ),
            ChatConversation(
                conversationId = "conv_app_1",
                studentId = "user_1",
                studentName = "Alex Rivera",
                studentEmail = "alex.rivera@example.com",
                conversationType = ConversationType.APPLICATION,
                title = "Application: University of Oxford",
                subTitle = "MSc Computer Science • Documents Under Review",
                counsellorId = "counsellor_elena",
                counsellorName = "Elena Vance",
                counsellorAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
                counsellorRole = "Admissions Specialist",
                isCounsellorOnline = true,
                applicationId = "app_1",
                applicationStatus = "Under Review",
                lastMessageText = "Your passport and transcripts have been verified for Oxford University application.",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                unreadCountStudent = 0
            )
        )
    )

    private val messagesState = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "conv_counsellor_1" to listOf(
                ChatMessage(
                    messageId = "msg_c_1",
                    conversationId = "conv_counsellor_1",
                    senderId = "counsellor_elena",
                    senderName = "Elena Vance",
                    senderRole = UserRole.COUNSELOR,
                    senderAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
                    messageType = ChatMessageType.TEXT,
                    text = "Hi! Welcome to Convoy. I'm here to help you with universities, scholarships, applications, and your study-abroad journey. How can I help you today?",
                    isDelivered = true,
                    isRead = false,
                    createdAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 30
                )
            ),
            "conv_support_1" to listOf(
                ChatMessage(
                    messageId = "msg_s_1",
                    conversationId = "conv_support_1",
                    senderId = "staff_support",
                    senderName = "Convoy Support Team",
                    senderRole = UserRole.ADMIN,
                    senderAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80",
                    messageType = ChatMessageType.TEXT,
                    text = "Welcome to Convoy Support! Feel free to ask about application issues, document requirements, or portal technical support.",
                    isDelivered = true,
                    isRead = true,
                    createdAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 120
                )
            ),
            "conv_app_1" to listOf(
                ChatMessage(
                    messageId = "msg_a_1",
                    conversationId = "conv_app_1",
                    senderId = "counsellor_elena",
                    senderName = "Elena Vance",
                    senderRole = UserRole.COUNSELOR,
                    senderAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
                    messageType = ChatMessageType.TEXT,
                    text = "Your passport and transcripts have been verified for Oxford University application.",
                    isDelivered = true,
                    isRead = true,
                    createdAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5
                )
            )
        )
    )

    private val internalNotesState = MutableStateFlow<Map<String, List<InternalNote>>>(
        mapOf(
            "conv_app_1" to listOf(
                InternalNote(
                    noteId = "note_1",
                    conversationId = "conv_app_1",
                    authorId = "counsellor_elena",
                    authorName = "Elena Vance",
                    content = "Student's IELTS score of 8.0 meets direct entry criteria for Oxford. Transcripts verified against UK ENIC standard.",
                    createdAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 4
                )
            )
        )
    )

    override fun fetchConversations(userId: String): Flow<List<ChatConversation>> =
        conversationsState.map { list ->
            list.filter { it.studentId == userId || userId == "user_1" }
                .sortedByDescending { it.lastMessageTimestamp }
        }

    override fun fetchAllConversationsForAdmin(): Flow<List<ChatConversation>> =
        conversationsState.map { list -> list.sortedByDescending { it.lastMessageTimestamp } }

    override fun fetchConversationById(conversationId: String): Flow<ChatConversation?> =
        conversationsState.map { list -> list.find { it.conversationId == conversationId } }

    override fun fetchMessages(conversationId: String): Flow<List<ChatMessage>> =
        messagesState.map { map -> map[conversationId] ?: emptyList() }

    override fun fetchInternalNotes(conversationId: String): Flow<List<InternalNote>> =
        internalNotesState.map { map -> map[conversationId] ?: emptyList() }

    override fun sendMessage(message: ChatMessage) {
        val convId = message.conversationId
        messagesState.update { currentMap ->
            val existing = currentMap[convId] ?: emptyList()
            currentMap + (convId to (existing + message))
        }

        conversationsState.update { list ->
            list.map { conv ->
                if (conv.conversationId == convId) {
                    val isStaff = message.senderRole == UserRole.COUNSELOR || message.senderRole == UserRole.ADMIN
                    conv.copy(
                        lastMessageText = if (message.attachment != null) "📎 ${message.attachment.fileName}" else message.text,
                        lastMessageTimestamp = message.createdAtTimestamp,
                        unreadCountStudent = if (isStaff) conv.unreadCountStudent + 1 else conv.unreadCountStudent,
                        unreadCountStaff = if (!isStaff) conv.unreadCountStaff + 1 else conv.unreadCountStaff
                    )
                } else conv
            }
        }
    }

    override fun markConversationAsRead(conversationId: String, userId: String) {
        conversationsState.update { list ->
            list.map { conv ->
                if (conv.conversationId == conversationId) {
                    if (conv.studentId == userId) {
                        conv.copy(unreadCountStudent = 0)
                    } else {
                        conv.copy(unreadCountStaff = 0)
                    }
                } else conv
            }
        }
        messagesState.update { map ->
            val list = map[conversationId] ?: return@update map
            map + (conversationId to list.map { msg -> if (msg.senderId != userId) msg.copy(isRead = true) else msg })
        }
    }

    override fun createOrGetCounsellorConversation(userId: String, userName: String, userEmail: String): String {
        val existing = conversationsState.value.find {
            it.studentId == userId && it.conversationType == ConversationType.COUNSELLOR
        }
        if (existing != null) return existing.conversationId

        val newId = "conv_counsellor_${UUID.randomUUID().toString().take(8)}"
        val newConv = ChatConversation(
            conversationId = newId,
            studentId = userId,
            studentName = userName,
            studentEmail = userEmail,
            conversationType = ConversationType.COUNSELLOR,
            title = "Elena Vance",
            subTitle = "Senior Study Abroad Counsellor",
            counsellorId = "counsellor_elena",
            counsellorName = "Elena Vance",
            counsellorAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
            counsellorRole = "Senior Study Abroad Counsellor",
            isCounsellorOnline = true,
            lastMessageText = "Hi! Welcome to Convoy. I'm here to help you with universities, scholarships, applications, and your study-abroad journey. How can I help you today?",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCountStudent = 1
        )
        val welcomeMsg = ChatMessage(
            conversationId = newId,
            senderId = "counsellor_elena",
            senderName = "Elena Vance",
            senderRole = UserRole.COUNSELOR,
            senderAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
            text = "Hi! Welcome to Convoy. I'm here to help you with universities, scholarships, applications, and your study-abroad journey. How can I help you today?",
            createdAtTimestamp = System.currentTimeMillis()
        )

        conversationsState.update { listOf(newConv) + it }
        messagesState.update { it + (newId to listOf(welcomeMsg)) }
        return newId
    }

    override fun createOrGetSupportConversation(userId: String, userName: String, userEmail: String, topic: String): String {
        val existing = conversationsState.value.find {
            it.studentId == userId && it.conversationType == ConversationType.SUPPORT && it.status == ConversationStatus.ACTIVE
        }
        if (existing != null) return existing.conversationId

        val newId = "conv_support_${UUID.randomUUID().toString().take(8)}"
        val newConv = ChatConversation(
            conversationId = newId,
            studentId = userId,
            studentName = userName,
            studentEmail = userEmail,
            conversationType = ConversationType.SUPPORT,
            title = "Convoy Support Desk",
            subTitle = topic.ifBlank { "24/7 Academic & Technical Support" },
            counsellorId = "staff_support",
            counsellorName = "Convoy Support Team",
            counsellorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80",
            counsellorRole = "Support Desk Lead",
            isCounsellorOnline = true,
            lastMessageText = "Welcome to Convoy Support! How can we assist you with $topic?",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCountStudent = 1
        )
        val welcomeMsg = ChatMessage(
            conversationId = newId,
            senderId = "staff_support",
            senderName = "Convoy Support Team",
            senderRole = UserRole.ADMIN,
            senderAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80",
            text = "Welcome to Convoy Support! How can we assist you with $topic?",
            createdAtTimestamp = System.currentTimeMillis()
        )

        conversationsState.update { listOf(newConv) + it }
        messagesState.update { it + (newId to listOf(welcomeMsg)) }
        return newId
    }

    override fun createOrGetApplicationConversation(
        userId: String,
        userName: String,
        userEmail: String,
        applicationId: String,
        universityName: String,
        programName: String
    ): String {
        val existing = conversationsState.value.find {
            it.studentId == userId && it.applicationId == applicationId
        }
        if (existing != null) return existing.conversationId

        val newId = "conv_app_${UUID.randomUUID().toString().take(8)}"
        val newConv = ChatConversation(
            conversationId = newId,
            studentId = userId,
            studentName = userName,
            studentEmail = userEmail,
            conversationType = ConversationType.APPLICATION,
            title = "Application: $universityName",
            subTitle = "$programName • Direct Application Desk",
            counsellorId = "counsellor_elena",
            counsellorName = "Elena Vance",
            counsellorAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
            counsellorRole = "Admissions Specialist",
            applicationId = applicationId,
            applicationStatus = "Under Review",
            lastMessageText = "Conversation started regarding your application to $universityName ($programName).",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCountStudent = 1
        )
        val welcomeMsg = ChatMessage(
            conversationId = newId,
            senderId = "counsellor_elena",
            senderName = "Elena Vance",
            senderRole = UserRole.COUNSELOR,
            senderAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
            text = "Hello $userName! This dedicated channel connects you directly with our admissions team for your $programName application at $universityName. Feel free to send questions or updated documents.",
            createdAtTimestamp = System.currentTimeMillis()
        )

        conversationsState.update { listOf(newConv) + it }
        messagesState.update { it + (newId to listOf(welcomeMsg)) }
        return newId
    }

    override fun addInternalNote(note: InternalNote) {
        internalNotesState.update { map ->
            val list = map[note.conversationId] ?: emptyList()
            map + (note.conversationId to (list + note))
        }
    }

    override fun updateConversationStatus(conversationId: String, status: ConversationStatus) {
        conversationsState.update { list ->
            list.map { conv ->
                if (conv.conversationId == conversationId) conv.copy(status = status) else conv
            }
        }
    }

    override fun assignConversationCounsellor(conversationId: String, counsellorId: String, counsellorName: String) {
        val counsellor = getCounsellors().find { it.id == counsellorId }
        conversationsState.update { list ->
            list.map { conv ->
                if (conv.conversationId == conversationId) {
                    conv.copy(
                        counsellorId = counsellorId,
                        counsellorName = counsellorName,
                        counsellorAvatarUrl = counsellor?.avatarUrl ?: conv.counsellorAvatarUrl,
                        counsellorRole = counsellor?.roleTitle ?: conv.counsellorRole
                    )
                } else conv
            }
        }
    }

    override fun deleteChatMessage(conversationId: String, messageId: String) {
        messagesState.update { map ->
            val list = map[conversationId] ?: return@update map
            map + (conversationId to list.map { msg -> if (msg.messageId == messageId) msg.copy(isDeleted = true, text = "This message was deleted") else msg })
        }
    }

    override fun reportOrBlockConversation(conversationId: String, isReported: Boolean, isBlocked: Boolean) {
        conversationsState.update { list ->
            list.map { conv ->
                if (conv.conversationId == conversationId) conv.copy(isReported = isReported, isBlocked = isBlocked) else conv
            }
        }
    }

    override fun getCounsellors(): List<CounsellorProfile> {
        return listOf(
            CounsellorProfile("counsellor_elena", "Elena Vance", "Senior Study Abroad Counsellor", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80", "UK, USA & Europe Admissions"),
            CounsellorProfile("counsellor_marcus", "Marcus Sterling", "Scholarships & Visa Specialist", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&q=80", "Full Funding & Visa Processing"),
            CounsellorProfile("counsellor_priya", "Priya Sharma", "Canada & Australia Specialist", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=300&q=80", "STEM Programs & Co-op Degrees"),
            CounsellorProfile("staff_support", "Convoy Support Team", "24/7 Technical & App Lead", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80", "General Technical & Document Desk")
        )
    }
}
