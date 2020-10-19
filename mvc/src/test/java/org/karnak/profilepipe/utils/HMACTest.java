package org.karnak.profilepipe.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;


class HMACTest {
    private static HMAC hmac1;
    private static HMAC hmac1_same;
    private static HMAC hmac2;
    private static HMAC hmac2_same;
    private static HMAC hmac3;
    private static HMAC hmac3_same;

    @BeforeAll
    static void beforeAll() {
        final byte[] HMAC_KEY1 = {67, -10, 60, -39, -82, -81, 34, 114, 48, -22, -42, 19, 91, -34, -10, 17};
        hmac1 = new HMAC(HMAC_KEY1);
        hmac1_same = new HMAC(HMAC_KEY1);
        final byte[] HMAC_KEY2 = {121, -97, 95, 98, -103, 120, -27, 59, 121, 24, -128, 105, 122, 114, -82, -23};
        hmac2 = new HMAC(HMAC_KEY2);
        hmac2_same = new HMAC(HMAC_KEY2);
        final byte[] HMAC_KEY3 = {0, 87, -96, 16, 6, 3, -59, 14, -31, 48, -21, 54, 67, 90, -1, 109};
        hmac3 = new HMAC(HMAC_KEY3);
        hmac3_same = new HMAC(HMAC_KEY3);
    }

    @ParameterizedTest
    @MethodSource("providerByteHash")
    void byteHash(HMAC hmac, String input, byte[] excepted) {
        assertArrayEquals(excepted, hmac.byteHash(input));
    }

    private static Stream<Arguments> providerByteHash() {
        byte[] outputExpected1 = {-67, 5, -58, 106, -102, -16, 19, -99, 122, 16, 34, 45, -94, -83, -17, -97, -103, -79, -110, -125, 32, -107, 56, 13, -27, -59, -112, 4, -54, 43, -123, -46};
        byte[] outputExpected2 = {-20, -117, 27, -66, 28, -51, -23, -101, -52, 61, 56, 39, -23, -115, 91, 19, 23, 22, -34, 13, -48, -19, -126, 68, -110, -109, 11, -39, -67, 14, 100, -111};
        byte[] outputExpected3 = {-36, -24, -66, -120, 26, 14, 120, -102, -20, -33, -39, -108, 14, 72, 3, 5, -40, 59, 96, 110, -123, 28, 19, -52, -107, -56, 94, 122, 54, 84, 39, 53};
        byte[] outputExpected4 = {73, 71, -56, 27, 65, 45, -21, -26, 31, -1, 40, -111, -33, -64, -125, 116, -10, 121, 13, -77, 103, 8, -30, -84, -6, -105, 100, -35, 81, -115, -14, 20};
        byte[] outputExpected5 = {5, 73, -41, 85, 14, 121, 65, 34, 103, 20, 24, 17, -98, 87, 110, -85, -79, -107, -63, -4, 72, -49, -80, -13, -58, 54, -31, 21, 91, 13, 89, 40};
        byte[] outputExpected6 = {112, -78, 92, 114, -54, 25, -24, 6, 15, 55, -107, -31, -26, -53, -101, 32, 120, 70, -41, -50, -47, -55, 13, 97, -114, 17, -47, -43, -127, 13, -77, 7};
        return Stream.of(
                Arguments.of(hmac1, "xbnMRs3W-6KM*sbM#hs8sCZrgbBPSw2CE?rnnUF=thnfG7bFw_fZvHe!Ka&B#", outputExpected1),
                Arguments.of(hmac1, "#Bv=mm683aN", outputExpected2),
                Arguments.of(hmac2, "xbnMRs3W-6KM*sbM#hs8sCZrgbBPSw2CE?rnnUF=thnfG7bFw_fZvHe!Ka&B#", outputExpected3),
                Arguments.of(hmac2, "#Bv=mm683aN", outputExpected4),
                Arguments.of(hmac3, "xbnMRs3W-6KM*sbM#hs8sCZrgbBPSw2CE?rnnUF=thnfG7bFw_fZvHe!Ka&B#", outputExpected5),
                Arguments.of(hmac3, "#Bv=mm683aN", outputExpected6)
        );
    }

    @ParameterizedTest
    @MethodSource("providerNotSameByteHash")
    void notSameByteHash(HMAC hmac1, HMAC hmac2, String input) {
        assertFalse(Arrays.equals(hmac1.byteHash(input), hmac2.byteHash(input)));
    }

    private static Stream<Arguments> providerNotSameByteHash() {
        return Stream.of(
                Arguments.of(hmac1, hmac2, "xbnMRs3W-6KM*sbM#hs8sCZrgbBPSw2CE?rnnUF=thnfG7bFw_fZvHe!Ka&B#"),
                Arguments.of(hmac1, hmac2, "#Bv=mm683aN"),
                Arguments.of(hmac1, hmac3, "xbnMRs3W-6KM*sbM#hs8sCZrgbBPSw2CE?rnnUF=thnfG7bFw_fZvHe!Ka&B#"),
                Arguments.of(hmac1, hmac3, "#Bv=mm683aN"),
                Arguments.of(hmac2, hmac3, "xbnMRs3W-6KM*sbM#hs8sCZrgbBPSw2CE?rnnUF=thnfG7bFw_fZvHe!Ka&B#"),
                Arguments.of(hmac2, hmac3, "#Bv=mm683aN")
        );
    }

    @ParameterizedTest
    @MethodSource("providerSameUIDHash")
    void sameUIDHash(HMAC hmac1, HMAC hmac2, String input) {
        assertEquals(hmac1.uidHash(input), hmac2.uidHash(input));
    }

    private static Stream<Arguments> providerSameUIDHash() {
        return Stream.of(
                Arguments.of(hmac1, hmac1_same, "2.25.163485808146406487370825160808855144872"),
                Arguments.of(hmac1, hmac1_same, "2.25.94864836973909141411232579544325294158"),
                Arguments.of(hmac2, hmac2_same, "2.25.234378532077629807026582121273495697860"),
                Arguments.of(hmac2, hmac2_same, "2.25.60425845025227825428941166719886325579"),
                Arguments.of(hmac3, hmac3_same, "2.25.174707390929794025815088409892000794305"),
                Arguments.of(hmac3, hmac3_same, "2.25.138226508601833892075134918123442900169")
        );
    }


    @ParameterizedTest
    @MethodSource("providerNotSameUIDHash")
    void notSameUIDHash(HMAC hmac1, HMAC hmac2, String input) {
        assertNotEquals(hmac1.uidHash(input), hmac2.uidHash(input));
    }

    private static Stream<Arguments> providerNotSameUIDHash() {
        return Stream.of(
                Arguments.of(hmac1, hmac2, "2.25.163485808146406487370825160808855144872"),
                Arguments.of(hmac1, hmac3, "2.25.94864836973909141411232579544325294158"),
                Arguments.of(hmac2, hmac3, "2.25.234378532077629807026582121273495697860"),
                Arguments.of(hmac1, hmac2, "2.25.60425845025227825428941166719886325579"),
                Arguments.of(hmac1, hmac3, "2.25.174707390929794025815088409892000794305"),
                Arguments.of(hmac2, hmac3, "2.25.138226508601833892075134918123442900169")
        );
    }

    @ParameterizedTest
    @MethodSource("providerUIDHash")
    void UIDHash(HMAC hmac, String input, String excepted) {
        assertEquals(excepted, hmac.uidHash(input));
    }

    private static Stream<Arguments> providerUIDHash() {
        return Stream.of(
                Arguments.of(hmac1, "2.25.163485808146406487370825160808855144872", "2.25.23780115187296296146791359390398077306"),
                Arguments.of(hmac1, "2.25.94864836973909141411232579544325294158", "2.25.328457347740766768121187744861290420070"),
                Arguments.of(hmac2, "2.25.234378532077629807026582121273495697860", "2.25.23566129958838497073884430386918567185"),
                Arguments.of(hmac2, "2.25.60425845025227825428941166719886325579", "2.25.272132527173693519191625211562117919822"),
                Arguments.of(hmac3, "2.25.174707390929794025815088409892000794305", "2.25.57642299140964991935616681121529560388"),
                Arguments.of(hmac3, "2.25.138226508601833892075134918123442900169", "2.25.31313976655925012646920868668665265450")
        );
    }

    @ParameterizedTest
    @MethodSource("providerScaleHash")
    void scaleHash(HMAC hmac, String value, int scaledMin, int scaledMax, double output){
        assertEquals(output, hmac.scaleHash(value, scaledMin, scaledMax));
    }

    private static Stream<Arguments> providerScaleHash() {
        return Stream.of(
                Arguments.of(hmac1, "", 10, 89, 43),
                Arguments.of(hmac2, "", 10, 90, 41),
                Arguments.of(hmac1, "PatientID", 0, 100, 77),
                Arguments.of(hmac1, "PatientID", 10, 100, 79)
        );
    }

    @ParameterizedTest
    @MethodSource("providerFormatKey")
    void formatKey(String key, String expected){
        assertEquals(expected, HMAC.formatKey(key));
    }

    private static Stream<Arguments> providerFormatKey() {
        return Stream.of(
                Arguments.of("43243243213412341234111111111111", "43243243-2134-1234-1234-111111111111"),
                Arguments.of("23731831da23daf33e83123456789012", "23731831-da23-daf3-3e83-123456789012")
        );
    }

    @ParameterizedTest
    @MethodSource("providerFormatKeyReplace")
    void formatKeyReplace(String key){
        assertEquals(key, HMAC.formatKey(key).replace("-", ""));
    }

    private static Stream<Arguments> providerFormatKeyReplace() {
        return Stream.of(
                Arguments.of("43243243213412341234111111111111"),
                Arguments.of("23731831da23daf33e83123456789012")
        );
    }
}