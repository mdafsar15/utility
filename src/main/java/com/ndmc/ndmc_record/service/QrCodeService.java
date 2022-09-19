package com.ndmc.ndmc_record.service;

public interface QrCodeService {

    public byte[] createQrCode(String text, int width, int height);
}
