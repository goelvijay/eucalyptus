/*************************************************************************
 * Copyright 2009-2012 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 ************************************************************************/

package com.eucalyptus.reporting.modules.storage;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.eucalyptus.auth.Accounts;
import com.eucalyptus.auth.AuthException;
import com.eucalyptus.auth.principal.User;
import com.eucalyptus.event.EventListener;
import com.eucalyptus.event.Listeners;
import com.eucalyptus.reporting.event.SnapShotEvent;
import com.eucalyptus.reporting.event_store.ReportingVolumeSnapshotEventStore;
import com.eucalyptus.reporting.domain.ReportingUserCrud;
import com.eucalyptus.reporting.domain.ReportingAccountCrud;

import com.google.common.base.Preconditions;

public class SnapShotUsageEventListener implements EventListener<SnapShotEvent> {

    private static Logger LOG = Logger
	    .getLogger(SnapShotUsageEventListener.class);

    public SnapShotUsageEventListener() {
    }

    public static void register() {
	Listeners.register(SnapShotEvent.class,
		new SnapShotUsageEventListener());
    }

    @Override
    public void fireEvent(@Nonnull final SnapShotEvent event) {
	Preconditions.checkNotNull(event, "Event is required");

	final long timeInMs = getCurrentTimeMillis();
	
	User user = null;

	try {
	    user = Accounts
		    .lookupUserById(event.getOwnerFullName().getUserId());

	    final ReportingAccountCrud reportingAccountDao = getReportingAccountCrud();

	    reportingAccountDao.createOrUpdateAccount(user.getAccount()
		    .getName(), user.getAccount().getAccountNumber());

	    final ReportingUserCrud reportingUserCrud = getReportingUserCrud();

	    reportingUserCrud.createOrUpdateUser(user.getUserId(), user
		    .getAccount().getAccountNumber(), user.getName());

	    final ReportingVolumeSnapshotEventStore eventStore = ReportingVolumeSnapshotEventStore
		    .getInstance();

	    switch (event.getActionInfo().getAction()) {
	    case SNAPSHOTCREATE:
		eventStore.insertCreateEvent(event.getUUID(), event.getSnapshotId(), timeInMs, event.getOwnerFullName()
			.getUserName(), event.getSizeGB());
		break;
	    case SNAPSHOTDELETE:
		eventStore.insertDeleteEvent(event.getUUID(), event.getSnapshotId(), event.getOwnerFullName().getUserName(), timeInMs);
		break;
	    }

	} catch (AuthException e) {
	    LOG.error("Unable fire snap shot reporting event", e.getCause());
	}

    }

    protected ReportingAccountCrud getReportingAccountCrud() {
	return ReportingAccountCrud.getInstance();
    }

    protected ReportingUserCrud getReportingUserCrud() {
	return ReportingUserCrud.getInstance();
    }

    protected ReportingVolumeSnapshotEventStore getReportingVolumeSnapshotEventStore() {
	return ReportingVolumeSnapshotEventStore.getInstance();
    }

    protected long getCurrentTimeMillis() {
	return System.currentTimeMillis();
    }

}
