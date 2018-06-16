/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2017 ForgeRock AS. All Rights Reserved
 */
/**
 * Portions Copyright 2018 Wiacts Inc.
 */
package com.nopassword.openam.module;

import java.security.Principal;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.sm.DNMapper;
import java.util.HashSet;
import org.forgerock.openam.core.CoreWrapper;

public class NoPasswordAuth extends AMLoginModule {

    // Name for the debug-log
    private final static Debug DEBUG = Debug.getInstance("NoPasswordAuth");
    private String username;

    // Orders defined in the callbacks file
    private final static int STATE_BEGIN = 1;
    private final static int STATE_AUTH = 2;
    private final static int STATE_ERROR = 3;

    // Errors properties
    private final static String USER_NOT_FOUND = "error-user-not-found";
    private final static String USER_EMAIL_NOT_FOUND = "error-user-email-not-found";
    private final static String CONTACT_ADMINISTRATOR = "error-contact-admin";
    private final static String INVALID_USER = "error-invalid-user";
    private final static String ACCESS_DENIED = "error-access-denied";

    private Map<String, Set<String>> options;
    private ResourceBundle bundle;
    private Map<String, String> sharedState;
    private String nopasswordLoginKey;
    private String authURL;

    public NoPasswordAuth() {
        super();
    }

    /**
     * This method stores service attributes and localized properties for later
     * use.
     *
     * @param subject
     * @param sharedState
     * @param options
     */
    @Override
    public void init(Subject subject, Map sharedState, Map options) {
        DEBUG.message("NoPasswordAuth::init");
        this.options = options;
        this.sharedState = sharedState;
        this.bundle = amCache.getResBundle("amAuthNoPasswordAuth", getLoginLocale());
        this.nopasswordLoginKey = CollectionHelper.getMapAttr(options, Constants.NOPASSWORD_LOGIN_KEY);
        this.authURL = CollectionHelper.getMapAttr(options, Constants.AUTH_URL);
    }

    @Override
    public int process(Callback[] callbacks, int state) throws LoginException {
        DEBUG.message("NoPasswordAuth::process state: {}", state);

        switch (state) {
            case STATE_BEGIN:
                // modify the UI and proceed to next state
                substituteUIStrings();
                return STATE_AUTH;

            case STATE_AUTH:
                // Get data from callbacks. Refer to callbacks XML file.
                NameCallback nc = (NameCallback) callbacks[0];
                username = nc.getName();

                // validate user
                if (username == null || "".equals(username)) {
                    setErrorText(INVALID_USER);
                    return STATE_ERROR;
                }

                String realm = DNMapper.orgNameToRealmName(getRequestOrg());
                AMIdentity userIdentity = new CoreWrapper().getIdentity(username, realm);

                if (userIdentity == null) {
                    setErrorText(USER_NOT_FOUND);
                    return STATE_ERROR;
                }

                String email = getEmail(userIdentity);

                if (email.isEmpty()) {
                    setErrorText(USER_EMAIL_NOT_FOUND);
                    return STATE_ERROR;
                }

                if (AuthHelper.authenticateUser(email, authURL, nopasswordLoginKey)) {
                    storeUsername(username);
                    return ISAuthConstants.LOGIN_SUCCEED;
                } else {
                    setErrorText(ACCESS_DENIED);
                    return STATE_ERROR;
                }
            case STATE_ERROR:
                return STATE_ERROR;
            default:
                throw new AuthLoginException("invalid state");
        }
    }

    @Override
    public Principal getPrincipal() {
        return new NoPasswordAuthPrincipal(username);
    }

    private void setErrorText(String err) throws AuthLoginException {
        // Receive correct string from properties and substitute the
        // header in callbacks order 3.
        substituteHeader(STATE_ERROR, bundle.getString(err));
    }

    private void substituteUIStrings() throws AuthLoginException {
        substituteHeader(STATE_AUTH, bundle.getString(Constants.UI_LOGIN_HEADER));
        replaceCallback(STATE_AUTH, 0, new NameCallback(
                bundle.getString(Constants.UI_USERANAME_PROMPT)));
    }

    private String getEmail(AMIdentity userIdentity) throws AuthLoginException {
        String email = "";
        try {
            Set<String> a = new HashSet<>();
            a.add("mail");
            a.add("email");
            a.add("dn");
            Map attrs = userIdentity.getAttributes(a);
            HashSet<String> emailSet = (HashSet) attrs.get("mail");

            //check mail and email attributes
            if (!emailSet.isEmpty()) {
                email = emailSet.iterator().next();
            } else {
                emailSet = (HashSet) attrs.get("email");
                if (!emailSet.isEmpty()) {
                    email = emailSet.iterator().next();
                }
            }

            //if both mail and email are empty, then get email from dn
            if (email == null || email.isEmpty()) {
                Set<String> dnSet = userIdentity.getAttribute("dn");
                email = getEmailFromDN(dnSet.iterator().next());    //userIdentity.getDn() return null!!!
            }
        } catch (Exception ex) {
            DEBUG.message("An error ocurred when getting user email: " + username, ex);
            setErrorText(CONTACT_ADMINISTRATOR);
        }
        return email;
    }

    private String getEmailFromDN(String dn) {
        if (dn == null || !dn.contains("dc=")) {
            return "";
        }

        String[] dc = dn.split(",dc=");
        int eqIdx = dn.indexOf('=');
        StringBuilder sb = new StringBuilder();
        sb.append(dn.substring(eqIdx + 1, dn.indexOf(',', eqIdx)))
                .append('@');

        for (int i = 1; i < dc.length; i++) {
            sb.append(dc[i]);

            if (i < dc.length - 1) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

}
