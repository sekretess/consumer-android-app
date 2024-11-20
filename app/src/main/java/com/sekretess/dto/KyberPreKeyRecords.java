package com.sekretess.dto;

import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

public class KyberPreKeyRecords {
    private final KyberPreKeyRecord lastResortKyberPreKeyRecord;
    private final KyberPreKeyRecord[] kyberPreKeyRecords;

    public KyberPreKeyRecords(KyberPreKeyRecord lastResortKyberPreKeyRecord, KyberPreKeyRecord[] kyberPreKeyRecords) {
        this.lastResortKyberPreKeyRecord = lastResortKyberPreKeyRecord;
        this.kyberPreKeyRecords = kyberPreKeyRecords;
    }


    public KyberPreKeyRecord getLastResortKyberPreKeyRecord() {
        return lastResortKyberPreKeyRecord;
    }


    public KyberPreKeyRecord[] getKyberPreKeyRecords() {
        return kyberPreKeyRecords;
    }
}
