package com.ipsis.scan.reporting.edition;

import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.reporting.edition.model.MissionType;
import com.ipsis.scan.utils.DateUtils;
import com.ipsis.scan.utils.SecurityUtils;
import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pobouteau on 9/30/16.
 */

public class MissionForms implements Serializable {

    private static final long serialVersionUID = 500001L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mInterpellationMissionType", MissionType.class),
            new ObjectStreamField("mMissionTypes", ArrayList.class)
    };

    private MissionType mInterpellationMissionType;

    private ArrayList<MissionType> mMissionTypes;

    public MissionForms(MissionType interpellationMissionType, ArrayList<MissionType> missionTypes) {
        mInterpellationMissionType = interpellationMissionType;
        mMissionTypes = missionTypes;
    }

    public MissionReport createReport(int id) {
        MissionType missionType = mMissionTypes.get(id);

        return createReport(missionType);
    }

    public MissionReport createReport(MissionType missionType) {
        if (missionType != null) {
            MissionReport report = new MissionReport();

            Calendar date = Calendar.getInstance();

            report.setFormId(missionType.getForm().getId());
            report.setDateTime(date.toString());
            report.setTempId(SecurityUtils.identifier());
            report.setLocalId(report.getTempId());
            report.setLocation(new MissionLocation());
            report.setLocationStart("");
            report.setLocationEnd("");
            report.setDateTimeStart(DateUtils.formatDate(Calendar.getInstance()));
            report.setDateTimeEnd("-1:-1");
            report.setAutomatic(0);

            report.setMissionType(missionType);

            return report;
        } else {
            return null;
        }
    }

    public MissionInterpellation createInterpellation(String tempId) {
        if (mInterpellationMissionType != null) {
            MissionInterpellation report = new MissionInterpellation();

            Calendar date = Calendar.getInstance();

            report.setLocalId(SecurityUtils.identifier());
            report.setFormId(mInterpellationMissionType.getForm().getId());
            report.setDateTime(date.toString());
            report.setTempId(tempId);
            report.setLocation(new MissionLocation());
            report.setLocationStart("");
            report.setLocationEnd("");
            report.setDateTimeStart(DateUtils.formatDate(Calendar.getInstance()));
            report.setDateTimeEnd("-1:-1");
            report.setAutomatic(0);

            report.setMissionType(mInterpellationMissionType);

            return report;
        } else {
            return null;
        }
    }

    public ArrayList<MissionType> getSurLigneForms() {
        ArrayList<MissionType> result = new ArrayList<>();

        for (MissionType missionType : mMissionTypes) {
            if (missionType.getForm().getLocationMode().equals("sur-ligne")) {
                result.add(missionType);
            }
        }

        return result;
    }

    public ArrayList<MissionType> getLieuFixeForms() {
        ArrayList<MissionType> result = new ArrayList<>();

        for (MissionType missionType : mMissionTypes) {
            if (missionType.getForm().getLocationMode().equals("lieu-fixe")) {
                result.add(missionType);
            }
        }

        return result;
    }

    public ArrayList<MissionType> getHorsReseauForms() {
        ArrayList<MissionType> result = new ArrayList<>();

        for (MissionType missionType : mMissionTypes) {
            if (missionType.getForm().getLocationMode().equals("hors-reseau")) {
                result.add(missionType);
            }
        }

        return result;
    }

    public MissionType getInterpellationMissionType() {
        return mInterpellationMissionType;
    }

    public ArrayList<MissionType> getMissionTypes() {
        return mMissionTypes;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }
}
