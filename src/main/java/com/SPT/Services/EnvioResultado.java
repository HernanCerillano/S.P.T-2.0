package com.SPT.Services;

public final class EnvioResultado {

    private final boolean ok;
    private final String sid;
    private final String error;

    private EnvioResultado(boolean ok, String sid, String error) {
        this.ok = ok;
        this.sid = sid;
        this.error = error;
    }

    public static EnvioResultado ok(String sid) {
        return new EnvioResultado(true, sid, null);
    }

    public static EnvioResultado fallo(String error) {
        return new EnvioResultado(false, null, error);
    }

    public boolean isOk() { return ok; }
    public String getSid() { return sid; }
    public String getError() { return error; }
}
