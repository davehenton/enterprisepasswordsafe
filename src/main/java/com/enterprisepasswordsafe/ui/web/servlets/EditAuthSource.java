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

package com.enterprisepasswordsafe.ui.web.servlets;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enterprisepasswordsafe.engine.database.AuthenticationSource;
import com.enterprisepasswordsafe.engine.database.AuthenticationSourceDAO;


/**
 * Servlet to send the user to the correct authentication source editing page.
 */

public final class EditAuthSource extends HttpServlet {
    /**
	 *
	 */
	private static final long serialVersionUID = 8681730788890408072L;

    /**
     * @see com.enterprisepasswordsafe.passwordsafe.servlets.NoResponseBaseServlet#serviceRequest
     *      (java.sql.Connection, javax.servlet.http.HTTPServletRequest)
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    		throws ServletException, IOException {
        String id = request.getParameter("id");

        try {
	        AuthenticationSource source = AuthenticationSourceDAO.getInstance().getById(id);
	        request.setAttribute("type", source.getJaasType());
	        request.setAttribute("name", source.getName());
	        request.setAttribute("id", source.getSourceId());
	    	request.setAttribute("parameters", source.getSourceOptions());
	    	request.setAttribute("notes", source.getSourceNotes());
        } catch(SQLException sqle) {
    		request.setAttribute("error_page", "/admin/AuthSources");
        	throw new ServletException("There was a problem obtaining the authentication source information.", sqle);
        }
        request.getRequestDispatcher("/admin/edit_authsource_configure.jsp").forward(request, response);
    }

    /**
     * @see javax.servlet.Servlet#getServletInfo()
     */
    @Override
	public String getServletInfo() {
        return "Directs the user to the correct authentication source editing page.";
    }
}
