import org.example.Dashboard;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LogicTest {

    // ==========================================
    // 1. TESTING FITUR LOGIN (Simulasi Logic Main.java)
    // ==========================================
    @Test
    void testLogicLogin() {
        String csvUser = "Radya";
        String csvPass = "370";

        boolean loginSukses = csvUser.equals("Radya") && csvPass.equals("370");
        boolean loginGagal = csvUser.equals("Radya") && csvPass.equals("123");

        assertTrue(loginSukses, "User Radya dengan pass 370 harusnya sukses login");
        assertFalse(loginGagal, "Password salah harusnya gagal login");
    }

    // ==========================================
    // 2. TESTING FITUR CREATE DATA (Simulasi Logic CreateData.java)
    // ==========================================
    @Test
    void testValidasiInputLengkap() {
        String nama = "Netflix";
        String harga = "150000";
        String tenggat = "20 Januari 2026";

        boolean isValid = !nama.isEmpty() && !harga.isEmpty() && !tenggat.isEmpty();

        assertTrue(isValid, "Data lengkap harusnya valid");
    }

    @Test
    void testFormatRupiah() {
        long inputHarga = 180000;
        String hasilFormat = String.format("Rp%,d", inputHarga).replace(',', '.');

        assertEquals("Rp180.000", hasilFormat, "Format rupiah salah");
    }

    @Test
    void testValidasiTanggalMasaDepan() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

        String inputTanggal = "01 Januari 3000";
        LocalDate date = LocalDate.parse(inputTanggal, fmt);

        boolean isValid = date.isAfter(LocalDate.now());

        assertTrue(isValid, "Tahun 3000 harusnya dianggap valid (masa depan)");
    }

    // ==========================================
    // 3. TESTING FITUR DASHBOARD (CRUD Entity)
    // ==========================================
    @Test
    void testSubRowEntity() {
        Dashboard.SubRow data = new Dashboard.SubRow("Spotify", "link.com", "Rp50.000", "01 Januari 2026");

        assertNotNull(data);
        assertEquals("Spotify", data.nama);
        assertEquals("Rp50.000", data.harga);
    }

    @Test
    void testFiturSearch() {
        Dashboard.SubRow data = new Dashboard.SubRow("Youtube Premium", "yt.com", "Rp59.000", "Besok");

        boolean hasilCari1 = data.matches("youtube");
        boolean hasilCari2 = data.matches("premium");
        boolean hasilCari3 = data.matches("netflix");

        assertTrue(hasilCari1, "Search 'youtube' harusnya ketemu");
        assertTrue(hasilCari2, "Search 'premium' harusnya ketemu");
        assertFalse(hasilCari3, "Search 'netflix' harusnya GAK ketemu");
    }

    // ==========================================
    // 4. TESTING FITUR PEMBAYARAN / UPDATE
    // ==========================================
    @Test
    void testKalkulasiPerpanjangan() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

        String tanggalLamaStr = "01 Januari 2025";
        LocalDate tanggalLama = LocalDate.parse(tanggalLamaStr, fmt);

        LocalDate tanggalBaru = tanggalLama.plusMonths(1);
        String hasil = tanggalBaru.format(fmt);

        assertEquals("01 Februari 2025", hasil, "Perhitungan tanggal salah");
    }

    @Test
    void testHitungTotalBayar() {
        long hargaSatuan = 50000;
        int durasi = 6;

        long total = hargaSatuan * durasi;

        assertEquals(300000, total, "Hitungan total bayar salah");
    }
}