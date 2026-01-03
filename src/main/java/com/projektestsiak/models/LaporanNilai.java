package com.projektestsiak.models;

public class LaporanNilai {
    private final String namaSiswa;
    private final String judulTugas;
    private final Double nilai;
    private final String komentar;

    public LaporanNilai(String namaSiswa, String judulTugas, Double nilai, String komentar) {
        this.namaSiswa = namaSiswa;
        this.judulTugas = judulTugas;
        this.nilai = nilai;
        this.komentar = komentar;
    }

    public String getNamaSiswa() {
        return namaSiswa;
    }

    public String getJudulTugas() {
        return judulTugas;
    }

    public Double getNilai() {
        return nilai;
    }

    public String getKomentar() {
        return komentar;
    }
}
