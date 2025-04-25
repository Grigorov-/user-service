package com.the.good.club.core.data;

import lombok.Getter;

public enum EmailTemplates {
    CORRELATION("Verify Your Account for The Good Club Platform",
            """
           <p>Hello,</p>
           <p>
           Thank you for registering with <strong>The Good Club</strong>.
           </p>
        
           <p>
           To complete your registration, please verify your identity by clicking the link below.
           </p>
           <p><a href="%s">👉 Verify Your Account:</a></p>
           <p>
           This will securely associate your DataU profile with your new account.
           </p>
        
           <p>
            <p>
            If you did not initiate this registration, you can safely ignore this message.
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
