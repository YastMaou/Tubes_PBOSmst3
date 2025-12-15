package com.projektestsiak.models;

public class MataKuliah {
    private int id;
    private String kode;
    private String nama;
    private int sks;
    private int semester;
    private String dosen;
    private double hargaSks;
    
    public MataKuliah(int id, String kode, String nama, int sks, int semester, String dosen, double hargaSks) {
        this.id = id;
        this.kode = kode;
        this.nama = nama;
        this.sks = sks;
        this.semester = semester;
        this.dosen = dosen;
        this.hargaSks = hargaSks;
    }
    
    // Getters
    public int getId() { return id; }
    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public int getSks() { return sks; }
    public int getSemester() { return semester; }
    public String getDosen() { return dosen; }
    public double getHargaSks() { return hargaSks; }
    public double getTotalBiaya() { return sks * hargaSks; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setKode(String kode) { this.kode = kode; }
    public void setNama(String nama) { this.nama = nama; }
    public void setSks(int sks) { this.sks = sks; }
    public void setSemester(int semester) { this.semester = semester; }
    public void setDosen(String dosen) { this.dosen = dosen; }
    public void setHargaSks(double hargaSks) { this.hargaSks = hargaSks; }
    
    @Override
    public String toString() {
        return kode + " - " + nama + " (" + sks + " SKS)";
    }
}