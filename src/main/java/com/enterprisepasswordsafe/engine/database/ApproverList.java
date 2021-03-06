/*
 * Copyright (c) 2017 Carbon Security Ltd. <opensource@carbonsecurity.co.uk>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.enterprisepasswordsafe.engine.database;

import com.enterprisepasswordsafe.proguard.ExternalInterface;

/**
 * Class holding the list of approvers for a particular item.
 */
public class ApproverList
    implements ExternalInterface {

	/**
	 * The state marker for a user who has approved a request.
	 */
	
	public static final String APPROVER_FLAG = "A";
	
	/**
	 * The state marker for a user who has denied a request.
	 */
	
	public static final String BLOCKER_FLAG = "B";
	
	/**
	 * The state marker for a user who has yet to make a block or approve.
	 */
	
	public static final String NOT_SELECTED_FLAG = "N";
	
}
