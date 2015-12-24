/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.security4;

import flex.messaging.MessageException;
import flex.messaging.security.SecurityException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SecurityExceptionTranslatorTests {

    private SecurityExceptionTranslator translator;

    @Before
    public void setUp() {
        this.translator = new SecurityExceptionTranslator();
    }

    @Test
	public void accessDeniedException() {

        String error = "Access is denied";

        MessageException ex = this.translator.translate(new AccessDeniedException(error));
        assertTrue("Should be a SecurityException", ex instanceof SecurityException);
        assertEquals(error, ex.getMessage());
        assertEquals(SecurityException.CLIENT_AUTHORIZATION_CODE, ex.getCode());
        assertTrue(ex.getRootCause() instanceof AccessDeniedException);

    }

    @Test
    public void authorizationException() {

        String error = "Invalid authentication";
        MessageException ex = this.translator.translate(new AuthenticationCredentialsNotFoundException(error));
        assertTrue("Should be a SecurityException", ex instanceof SecurityException);
        assertEquals(error, ex.getMessage());
        assertEquals(SecurityException.CLIENT_AUTHENTICATION_CODE, ex.getCode());
        assertTrue(ex.getRootCause() instanceof AuthenticationException);

    }

    @Test
    public void unknownExceptionPassthrough() {

        MessageException expected = new MessageException();
        assertNull(this.translator.translate(expected));
    }

}
