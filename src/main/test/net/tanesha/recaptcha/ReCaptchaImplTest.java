/*
 * Copyright 2007 Soren Davidsen, Tanesha Networks
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tanesha.recaptcha;

import java.util.Properties;

import net.tanesha.recaptcha.http.HttpLoader;
import junit.framework.TestCase;

public class ReCaptchaImplTest extends TestCase {

	ReCaptchaImpl r;
	MockHttpLoader l;
	protected void setUp() throws Exception {
		r = new ReCaptchaImpl();
		l = new MockHttpLoader();
		
		r.setIncludeNoscript(false);
		r.setPrivateKey("testing");
		r.setPublicKey("testing");
		r.setRecaptchaServer(ReCaptchaImpl.HTTPS_SERVER);
		r.setHttpLoader(l);
	}

	public void testCreateCaptchaHtml() {

		String html = r.createRecaptchaHtml(null, null);
		assertTrue(html.indexOf("<script") != -1);

		r.setIncludeNoscript(true);
		
		assertTrue(r.createRecaptchaHtml(null, null).indexOf("<noscript>") != -1);
		
		
		String html2 = r.createRecaptchaHtml("The Error", null);
		assertTrue(html2.indexOf("&error=The+Error") != -1);
		
		Properties options = new Properties();
		options.setProperty("theme", "mytheme");
		options.setProperty("tabindex", "1");
		String html3 = r.createRecaptchaHtml("The Error", options);
		assertTrue(html3.indexOf("theme:'mytheme'") != -1);
		assertTrue(html3.indexOf("tabindex:'1'") != -1);
		assertTrue(html3.indexOf(",") != -1);
		

		// check the shortcut
		String html4 = r.createRecaptchaHtml("Some Error", "othertheme", new Integer(3));
		assertTrue(html4.indexOf("theme:'othertheme'") != -1);
		assertTrue(html4.indexOf("tabindex:'3'") != -1);
		assertTrue(html4.indexOf(",") != -1);

		
	}
	
	public void testCheckAnswer() {
		l.setNextReply("true\nnone");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertTrue(reponse.isValid());
		assertEquals(null, reponse.getErrorMessage());
	}

	public void testCheckAnswer_02() {
		l.setNextReply("true\n");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertTrue(reponse.isValid());
	}

	public void testCheckAnswer_03() {
		l.setNextReply("true");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertTrue(reponse.isValid());
	}

	public void testCheckAnswer_04() {
		l.setNextReply("false");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertFalse(reponse.isValid());
		assertEquals("recaptcha4j-missing-error-message", reponse.getErrorMessage());

	}

	public void testCheckAnswer_05() {
		l.setNextReply("nottrue");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertFalse(reponse.isValid());
		assertEquals("recaptcha4j-missing-error-message", reponse.getErrorMessage());

	}

	public void testCheckAnswer_06() {
		l.setNextReply("false\nblabla");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertFalse(reponse.isValid());
		assertEquals("blabla", reponse.getErrorMessage());

	}

	public void testCheckAnswer_07() {
		l.setNextReply("false\nblabla\n\n");
		
		ReCaptchaResponse reponse = r.checkAnswer("123.123.123.123", "abcdefghijklmnop", "response");
		
		assertFalse(reponse.isValid());
		assertEquals("blabla", reponse.getErrorMessage());

	}

	public class MockHttpLoader implements HttpLoader {
		
		String url;
		String postdata;
		private String reply;
		
		public void setNextUrl(String url) {
			this.url = url;
		}
		public void setNextPostdata(String postdata) {
			this.postdata = postdata;
		}
		public void setNextReply(String reply) {
			this.reply = reply;
		}
		
		public String httpGet(String url) {
			if (this.url != null)
				assertEquals(this.url, url);
			
			return reply;
		}
		public String httpPost(String url, String postdata) {
			if (this.url != null)
				assertEquals(this.url, url);

			if (this.postdata != null)
				assertEquals(this.postdata, postdata);
			
			return reply;
		}
	}
	
}
