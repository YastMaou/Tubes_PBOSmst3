package com.projektestsiak.models;

import java.time.LocalDateTime;

public class Tugas {
    private int id;
    private int mataKuliahId;
    private String mataKuliahNama;
    private String judul;
    private String deskripsi;
    private LocalDateTime deadline;
    private String filePath;
    private boolean submitted;
    private Double nilai;
    private String komentar;
    
    public Tugas(int id, int mataKuliahId, String mataKuliahNama, String judul, 
                 String deskripsi, LocalDateTime deadline, String filePath) {
        this.id = id;
        this.mataKuliahId = mataKuliahId;
        this.mataKuliahNama = mataKuliahNama;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.deadline = deadline;
        this.filePath = filePath;
        this.submitted = false;
        this.nilai = null;
    }
    
    // Getters
    public int getId() { return id; }
    public int getMataKuliahId() { return mataKuliahId; }
    public String getMataKuliahNama() { return mataKuliahNama; }
    public String getJudul() { return judul; }
    public String getDeskripsi() { return deskripsi; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getFilePath() { return filePath; }
    public boolean isSubmitted() { return submitted; }
    public Double getNilai() { return nilai; }
    public String getKomentar() { return komentar; }
    
    public String getStatus() {
        if (submitted) {
            return "Terkumpul" + (nilai != null ? " (Nilai: " + nilai + ")" : "");
        } else {
            return "Belum dikumpulkan";
        }
    }
    
    public boolean isTerlambat() {
        return LocalDateTime.now().isAfter(deadline) && !submitted;
    }
    
    public String getDeadlineFormat() {
        return deadline.toString().replace("T", " ");
    }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setMataKuliahId(int mataKuliahId) { this.mataKuliahId = mataKuliahId; }
    public void setMataKuliahNama(String mataKuliahNama) { this.mataKuliahNama = mataKuliahNama; }
    public void setJudul(String judul) { this.judul = judul; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
    public void setNilai(Double nilai) { this.nilai = nilai; }
    public void setKomentar(String komentar) { this.komentar = komentar; }
}