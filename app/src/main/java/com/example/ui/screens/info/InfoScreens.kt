package com.example.ui.screens.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun AboutConvoyModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_convoy_symbol),
                    contentDescription = "Convoy Logo",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("About Convoy: Study Abroad", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Your Journey to Global Education Starts Here", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Convoy: Study Abroad is a global education platform connecting students directly with universities, funding opportunities, and scholarship providers worldwide.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Our Key Pillars:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Transparent University Discovery & Global Rankings\n• Verified Fully-Funded & Merit Scholarship Directory\n• Unified One-Click Application Processing\n• Secure Digital Document Locker", fontSize = 12.sp, lineHeight = 18.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Version: 1.0.0 (Foundation Build)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PrivacyPolicyModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Convoy: Study Abroad", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("Last Updated: February 2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Convoy: Study Abroad (\"Convoy\", \"we\", \"our\", or \"us\") respects your privacy. This Privacy Policy explains what information we collect, how we use it, how we protect it, and the choices available to you when you use the Convoy application.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("1. INFORMATION WE COLLECT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When you create an account using Google Sign-In, we may receive basic account information such as:\n• Name\n• Email address\n• Profile information made available through Google authentication\n\nWhen you use Convoy's services, you may voluntarily provide:\n• Country of residence\n• Intended study level\n• Preferred countries\n• Field of study\n• University and scholarship preferences\n• Application information\n• Academic information\n• Passport or identification documents\n• Certificates and other supporting documents\n• Contact information\n• Information you provide when contacting Convoy\n\nWe only request application documents when they are necessary for an application or service requested by the user.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("2. HOW WE USE YOUR INFORMATION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We may use your information to:\n• Create and manage your Convoy account\n• Help you discover universities and scholarships\n• Provide study-abroad information\n• Process applications requested by you\n• Review and organize application documents\n• Communicate with you about your applications\n• Respond to support or Contact Us requests\n• Improve Convoy's services\n• Maintain security and prevent misuse\n• Comply with applicable legal obligations",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("3. APPLICATION DOCUMENTS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Documents uploaded for an application are provided voluntarily by the user.\n\nWe use these documents only for the purposes explained to the user, including application assistance and processing requested by the user.\n\nUsers should only upload documents that belong to them or that they are legally authorized to provide.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("4. SHARING OF INFORMATION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We do not sell users' personal information.\n\nInformation may be shared when necessary to provide a service requested by the user, including with:\n• Universities or educational institutions selected by the user\n• Authorized application partners or service providers involved in the requested application\n• Technical service providers required to operate Convoy\n• Authorities or other parties where legally required\n\nWe will not share application documents with an institution for an application unless this is necessary for the service requested by the user or otherwise permitted by applicable law.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("5. ADVERTISING", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Convoy may use Google AdMob or other advertising services.\n\nAdvertising providers may process certain information according to their own privacy policies and applicable consent requirements.\n\nConvoy does not provide advertisers with users' application documents.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("6. DATA SECURITY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We take reasonable technical and organizational measures to protect personal information against unauthorized access, loss, misuse, alteration, or disclosure.\n\nHowever, no internet-based service can guarantee absolute security.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("7. DATA RETENTION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We retain information only for as long as reasonably necessary to provide our services, process applications, comply with legal obligations, resolve disputes, and maintain appropriate business records.\n\nApplication documents may be retained for as long as necessary for the requested application or service, subject to applicable legal requirements.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("8. YOUR RIGHTS AND CHOICES", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Depending on applicable law, you may have rights to:\n• Access your personal information\n• Request correction of inaccurate information\n• Request deletion of your account or information\n• Withdraw certain consents\n• Request information about how your data is used\n\nTo request assistance with your personal information, contact us through the Contact Us section of the Convoy application.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("9. CHILDREN'S PRIVACY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Convoy is intended for users who are legally permitted to use the service under applicable law.\n\nWe do not knowingly collect personal information from children where such collection is prohibited by applicable law.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("10. THIRD-PARTY SERVICES", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Convoy may use third-party services such as Google Sign-In, cloud infrastructure, analytics, advertising, and other technical services.\n\nThese services may process information according to their respective privacy policies and terms.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("11. CHANGES TO THIS PRIVACY POLICY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We may update this Privacy Policy from time to time.\n\nWhen important changes are made, we will provide appropriate notice within the application or through other appropriate communication.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("12. CONTACT US", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you have questions about this Privacy Policy, your personal information, or your account, please contact Convoy through the Contact Us section of the application.\n\nBy using Convoy after being presented with this Privacy Policy and applicable consent options, you acknowledge that you have read and understood this Privacy Policy.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("I Understand & Agree")
            }
        }
    )
}

@Composable
fun TermsConditionsModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "By creating an account or submitting an application through Convoy, you agree to the following terms:",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("1. Accuracy of Information", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Students are responsible for ensuring that all academic transcripts, test scores, and personal identification submitted are authentic.", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text("2. Application Submissions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Convoy pre-screens applications prior to university delivery. Final admission decisions rest solely with the university board.", fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Accept Terms")
            }
        }
    )
}

@Composable
fun HelpSupportModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Help & Support", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Q: How long does application review take?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("A: Convoy processes pre-screening within 48 hours. University responses vary from 2-6 weeks.", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Q: Are there application fees on Convoy?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("A: Convoy is free for students. Official university application fees (if applicable) are shown transparently before submission.", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Need direct assistance?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Contact our global student advisors at support@convoy.edu", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Help")
            }
        }
    )
}
