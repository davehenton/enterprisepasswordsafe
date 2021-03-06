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

import com.enterprisepasswordsafe.engine.database.schema.AccessControlDAOInterface;
import com.enterprisepasswordsafe.engine.utils.KeyUtils;
import com.enterprisepasswordsafe.proguard.ExternalInterface;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Data access object for GroupAccessControl objects.
 */

public class GroupAccessControlDAO
    implements AccessControlDAOInterface<Group, GroupAccessControl>, ExternalInterface {

    /**
     * The fields needed to construct a GroupAccessControl from a ResultSet.
     */

    public static final String GAC_FIELDS = " gac.item_id, gac.mkey, gac.rkey, gac.group_id ";

    /**
     * The number of columns in the group access control data.
     */

    public static final int GAC_FIELD_COUNT = 1 + AccessControl.ACCESS_CONTROL_FIELD_COUNT;

    /**
     * The SQL to delete a GAC.
     */

    private static final String DELETE_SQL =
            "DELETE FROM group_access_control WHERE group_id = ? AND item_id = ?";

    /**
     * The SQL to delete all GACs for an item.
     */

    private static final String DELETE_ALL_FOR_ITEM_SQL =
            "DELETE FROM group_access_control WHERE item_id = ? AND group_id <> '"+Group.ADMIN_GROUP_ID+"'";

    /**
     * SQL To get the GAC allowing a user full access to a item.
     */

    private static final String GET_GROUP_FOR_FULL_GAC_SQL =
            "SELECT " + GAC_FIELDS + " FROM group_access_control gac, membership mem "
            + "WHERE mem.user_id = ? AND gac.item_id = ?  AND gac.group_id = mem.group_id "
            + "  AND gac.rkey IS NOT NULL AND gac.mkey IS NOT NULL ORDER BY gac.group_id";

    /**
     * SQL To get the GAC allowing a user full access to a item.
     */

    private static final String GET_GROUP_FOR_FULL_GAC_INCLUDING_DISABLED_SQL =
            "SELECT " + GAC_FIELDS + " FROM group_access_control gac, membership mem "
            + "WHERE mem.user_id = ? AND gac.item_id = ? AND gac.group_id = mem.group_id "
            + "  AND gac.rkey IS NOT NULL AND gac.mkey IS NOT NULL ";

    /**
     * SQL To get the GAC allowing a user read access to a item.
     */

    private static final String GET_GROUP_FOR_GAC_SQL =
            "SELECT " + GAC_FIELDS + "  FROM group_access_control gac, membership mem "
            + " WHERE mem.user_id = ? AND mem.group_id = gac.group_id AND gac.item_id = ? AND gac.rkey IS NOT NULL ";

    /**
     * SQL To get the GAC allowing users access to a item.
     */

    private static final String GET_GROUP_FOR_GAC_INCLUDING_DISABLED_SQL =
    		"SELECT "+ GAC_FIELDS + "  FROM group_access_control gac, membership mem "
            + "WHERE mem.user_id = ? AND gac.item_id = ? AND gac.group_id = mem.group_id AND gac.rkey IS NOT NULL "
            + "  AND gac.mkey IS NULL ";

    /**
     * SQL To get the GAC for a specific user and specific group.
     */

    private static final String GET_GAC_FOR_GROUP_SQL =
    		"SELECT "+ GAC_FIELDS + " FROM group_access_control gac "
            + " WHERE gac.group_id = ? AND gac.item_id = ? AND gac.rkey IS NOT NULL ";

    /**
     * The SQL to get all group access controls for all groups with access to
     * this password.
     */

    private static final String GET_GAC_SUMMARIES_GAR_SQL =
              "SELECT gar.role" + "  FROM group_access_roles gar WHERE gar.item_id = ? AND gar.actor_id = ?";

    /**
     * The SQL to get all group access controls for all groups with access to
     * this password.
     */

    private static final String GET_GAC_SUMMARIES_GAC_SQL =
              "SELECT gac.rkey, gac.mkey FROM group_access_control gac "
            +  "WHERE gac.item_id = ? AND gac.group_id = ? AND gac.rkey IS NOT NULL ";


    /**
     * The SQL statement to insert a group access control into the database.
     */

    private static final String WRITE_GAC_SQL =
            "INSERT INTO group_access_control(group_id, item_id, rkey, mkey) VALUES ( ?, ?, ?, ?)";

	/**
	 * Private constructor to prevent instantiation
	 */

	private GroupAccessControlDAO() {
		super();
	}


    public GroupAccessControl getReadGac(final User theUser, final String itemId)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
	    return getGac(GET_GROUP_FOR_GAC_SQL, theUser, itemId);
    }

    public GroupAccessControl getGac(final User theUser, final AccessControledObject item)
        throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        return getGac(theUser, item.getId());
    }

    public GroupAccessControl getGac(final User theUser, final String itemId)
        throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        return getGacWork(theUser, itemId, GET_GROUP_FOR_FULL_GAC_SQL, GET_GROUP_FOR_GAC_SQL);
    }

    public GroupAccessControl getGac(final User user, final Group group, final AccessControledObject item)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        if(item == null) {
            return null;
        }

        return getGac(user, group, item.getId());
    }

    public GroupAccessControl getGac(final Group group, final Password password)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        if(password == null) {
            return null;
        }

        return getGac(group, password.getId());
    }

    public GroupAccessControl getGac(final User user, final Group group, final String itemId)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        if(user == null || group == null || itemId == null) {
            return null;
        }

        if( group.getAccessKey() == null ) {
            Membership membership = MembershipDAO.getInstance().getMembership(user, group.getGroupId());
            group.updateAccessKey(membership);
        }

        return getGroupAccessControlForGroup(group, itemId);
    }


    public GroupAccessControl getGacEvenIfDisabled(final User theUser, final AccessControledObject item)
        throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        return getGacEvenIfDisabled(theUser, item.getId());
    }

    public GroupAccessControl getGacEvenIfDisabled(final User theUser,
    		final String itemId)
        throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        return getGacWork(theUser, itemId, GET_GROUP_FOR_FULL_GAC_INCLUDING_DISABLED_SQL, GET_GROUP_FOR_GAC_INCLUDING_DISABLED_SQL);
    }

    private GroupAccessControl getGacWork(final User theUser, final String itemId, final String fullSql, final String readOnlySql)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        // Check for invalid parameters
        if (theUser == null || itemId == null) {
            return null;
        }

        GroupAccessControl fullAccessControl = getGac(fullSql, theUser, itemId);
        return fullAccessControl == null ? getGac(readOnlySql, theUser, itemId) : fullAccessControl;
    }

    private GroupAccessControl getGac(final String sql, final User theUser, final String itemId)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        try(PreparedStatement ps = BOMFactory.getCurrentConntection().prepareStatement(sql)) {
            ps.setString(1, theUser.getUserId());
            ps.setString(2, itemId);
            ps.setMaxRows(1);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String groupId = rs.getString(4);
                    Group group = GroupDAO.getInstance().getByIdDecrypted(groupId, theUser);
                    return new GroupAccessControl(rs, 1, group);
                }
            }
        }

        return null;
    }


    public GroupAccessControl getGac(final Group group, final String passwordId)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
    	if(group == null || passwordId == null) {
    		return null;
    	}

    	if( group.getAccessKey() == null ) {
    		throw new GeneralSecurityException("Attempt to decrypt group with encoded group");
    	}

        return getGroupAccessControlForGroup(group, passwordId);
    }

    private GroupAccessControl getGroupAccessControlForGroup(Group group, String passwordId)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        try(PreparedStatement ps = BOMFactory.getCurrentConntection().prepareStatement(GET_GAC_FOR_GROUP_SQL)) {
            ps.setString(1, group.getGroupId());
            ps.setString(2, passwordId);
            ps.setMaxRows(1);
            try(ResultSet rs = ps.executeQuery()) {
	            return rs.next() ? new GroupAccessControl(rs, 1, group) : null;
            }
        }
    }

    public void delete(final GroupAccessControl gac) throws SQLException {
        try(PreparedStatement ps = BOMFactory.getCurrentConntection().prepareStatement(DELETE_SQL)) {
            ps.setString(1, gac.getGroupId());
            ps.setString(2, gac.getItemId());
            ps.executeUpdate();
        }
    }

    public void deleteAllForItem(AccessControledObject aco)
        throws SQLException {
        try(PreparedStatement ps = BOMFactory.getCurrentConntection().prepareStatement(DELETE_ALL_FOR_ITEM_SQL)) {
            ps.setString(1, aco.getId());
            ps.executeUpdate();
        }
    }

    public GroupAccessControl create(Group group, AccessControledObject item,
    		boolean allowRead, boolean allowModify)
    	throws SQLException, UnsupportedEncodingException, GeneralSecurityException {
    	return create(group, item, allowRead, allowModify, true);
    }


    public GroupAccessControl create(Group group, AccessControledObject item,
    		boolean allowRead, boolean allowModify, boolean writeToDatabase)
    	throws SQLException, UnsupportedEncodingException, GeneralSecurityException {
    	PrivateKey modifyKey = allowModify ? item.getModifyKey() : null;
    	GroupAccessControl gac =
    			new GroupAccessControl( group.getGroupId(), item.getId(), modifyKey, item.getReadKey() );
    	if(writeToDatabase) {
    		write(group, gac);
    	}
    	return gac;
    }

    public void write(final Group group, final GroupAccessControl gac)
    	throws SQLException, UnsupportedEncodingException, GeneralSecurityException {
        try(PreparedStatement ps = BOMFactory.getCurrentConntection().prepareStatement(WRITE_GAC_SQL)) {
            ps.setString(1,	gac.getGroupId());
            ps.setString(2,	gac.getItemId());
            ps.setBytes(3,	KeyUtils.encryptKey(gac.getReadKey(), group.getKeyEncrypter()));
            ps.setBytes(4, 	KeyUtils.encryptKey(gac.getModifyKey(), group.getKeyEncrypter()));
            ps.executeUpdate();
        }
    }

    public Set<AccessSummary> getSummaries(final AccessControledObject item)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
        Set<AccessSummary> summaries = new TreeSet<AccessSummary>();

        Connection conn = BOMFactory.getCurrentConntection();
    	try(PreparedStatement gacPS = conn.prepareStatement(GET_GAC_SUMMARIES_GAC_SQL)) {
        	try(PreparedStatement garPS = conn.prepareStatement(GET_GAC_SUMMARIES_GAR_SQL)) {
	    		gacPS.setString(1, item.getId());
	    		garPS.setString(1, item.getId());

	    		for(Group thisGroup :  GroupDAO.getInstance().getAll()) {
		    		boolean canRead = false;
		    		boolean canModify = false;
		    		gacPS.setString(2, thisGroup.getGroupId());
		    		try(ResultSet rs = gacPS.executeQuery()) {
		    			if( rs.next() ) {
		    				rs.getString(1);	// Read the read key
		    				canRead = (rs.wasNull() == false);
		    				rs.getString(2);	// Read the modify key
		    				canModify = (rs.wasNull() == false);
		    			}
		    		}

		    		boolean canApproveRARequest = false;
		    		boolean canViewHistory = false;
		    		garPS.setString(2, thisGroup.getGroupId());
		    		try(ResultSet rs = garPS.executeQuery()) {
		    			while( rs.next() ) {
		    				String role = rs.getString(1);
		    				if( rs.wasNull() ) {
		    					continue;
		    				}

		    				if( role.equals(AccessRole.APPROVER_ROLE) ) {
		    					canApproveRARequest = true;
		    				} else if ( role.equals(AccessRole.HISTORYVIEWER_ROLE) ) {
		    					canViewHistory = true;
		    				}
		    			}
		    		}

	            	AccessSummary gas = new AccessSummary( thisGroup.getGroupId(), thisGroup.getGroupName(),
                            canRead, canModify, canApproveRARequest, canViewHistory);
	            	summaries.add(gas);
		    	}

	    		return summaries;
        	}
    	}
    }

    public void update(final Group group, final GroupAccessControl gac)
            throws SQLException, GeneralSecurityException, UnsupportedEncodingException {
// TODO: Look at improving update
    	delete(gac);
    	write(group, gac);
    }

    //------------------------

    private static class InstanceHolder {
    	static final GroupAccessControlDAO INSTANCE = new GroupAccessControlDAO();
    }

    public static GroupAccessControlDAO getInstance() {
		return InstanceHolder.INSTANCE;
    }
}
