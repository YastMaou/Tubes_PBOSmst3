package com.projektestsiak.models;

import java.time.LocalTime;

public class Jadwal {
    private int id;
    private int mataKuliahId;
    private String mataKuliahNama;
    private String hari;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private String ruangan;
    
    public Jadwal(int id, int mataKuliahId, String mataKuliahNama, String hari, 
                  LocalTime jamMulai, LocalTime jamSelesai, String ruangan) {
        this.id = id;
        this.mataKuliahId = mataKuliahId;
        this.mataKuliahNama = mataKuliahNama;
        this.hari = hari;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.ruangan = ruangan;
    }
    
    // Getters
    public int getId() { return id; }
    public int getMataKuliahId() { return mataKuliahId; }
    public String getMataKuliahNama() { return mataKuliahNama; }
    public String getHari() { return hari; }
    public LocalTime getJamMulai() { return jamMulai; }
    public LocalTime getJamSelesai() { return jamSelesai; }
    public String getRuangan() { return ruangan; }
    public String getJamFormat() { return jamMulai + " - " + jamSelesai; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setMataKuliahId(int mataKuliahId) { this.mataKuliahId = mataKuliahId; }
    public void setMataKuliahNama(String mataKuliahNama) { this.mataKuliahNama = mataKuliahNama; }
    public void setHari(String hari) { this.hari = hari; }
    public void setJamMulai(LocalTime jamMulai) { this.jamMulai = jamMulai; }
    public void setJamSelesai(LocalTime jamSelesai) { this.jamSelesai = jamSelesai; }
    public void setRuangan(String ruangan) { this.ruangan = ruangan; }
}