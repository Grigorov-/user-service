package com.the.good.club.core.data;

import lombok.Getter;

public enum EmailTemplates {
    CORRELATION("Connect your Good Club account with DataU",
            """
           <p>Hello,</p>
           <p>
           <strong>Welcome to The Good Club! 🌱</strong>
           </p>
        
           <p>
           To complete your registration, please connect your account with DataU.
           </p>
        
           <p>
           DataU is a trusted European service that helps you:
           </p>
           <ul>
               <li>Stay in control of your personal data</li>
               <li>Easily see and manage the consents you give</li>
               <li>Be sure everything is protected under GDPR rules</li>
           </ul>
           <p><a href="%s">👉 Connect with DataU:</a></p>
           <p>
           This quick step links your Good Club account to DataU, so you always know how your data is used and can change your preferences anytime.            </p>
           </p>
           <p>
           If you didn’t register at Good Club, you can safely ignore this message.
           </p>
           <p>
            Best regards,<br>
            The Good Club Team
            </p>
        """),
    PERMISSION("Consent Required: Approve Access and Accept Terms",
            """ 
     <p>Hello,</p>
 
     <p>To activate your account on <strong>The Good Club</strong> platform, please complete the following steps:</p>
 
     <ol>
         <li>Review and approve the required permissions.</li>
         <li>Read and accept our Terms and Conditions.</li>
     </ol>
 
    <p>👉 <a href="%s">Grant Consent and Accept Terms</a></p>
 
    <p>Your account will remain inactive until these actions are completed.</p>
 
    <p>
    </p>
    <p>If you have any questions, feel free to contact us.</p>
 
     <p>
         Thank you,<br>
         The Good Club Team
     </p>
 """);

    @Getter
    private final String subject;
    private final String body;


    EmailTemplates(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String formattedBody(Object... args) {
        return body.formatted(args);
    }
}
