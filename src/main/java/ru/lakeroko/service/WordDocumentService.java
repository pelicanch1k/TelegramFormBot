package ru.lakeroko.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import ru.lakeroko.model.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WordDocumentService {
    private final User user;
    private final String fileName;

    public WordDocumentService(User user) {
        this.user = user;
        fileName = "src/main/resources/Docxfiles/" + user.getUsername() + ".docx";
    }

    public byte[] createDocx() throws IOException, InvalidFormatException {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("Ваши данные");
        titleRun.setBold(true);
        titleRun.setFontSize(20);

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setText("Данный документ содержит ваши персональные данные");
        paragraphRun.setFontSize(14);

        XWPFTable table = document.createTable(getRows(), 2);
        int counter = 0;

        table.getRow(counter).getCell(0).setText("Имя");
        table.getRow(counter).getCell(1).setText(user.getFirstName());
        counter++;

        table.getRow(counter).getCell(0).setText("Фамилия");
        table.getRow(counter).getCell(1).setText(user.getLastName());
        counter++;

        if (user.getMiddleName() != null) {
            table.getRow(counter).getCell(0).setText("Отчество");
            table.getRow(counter).getCell(1).setText(user.getMiddleName());
            counter++;
        }

        table.getRow(counter).getCell(0).setText("Дата рождения");
        table.getRow(counter).getCell(1).setText(user.getBirthDate().toString());
        counter++;

        table.getRow(counter).getCell(0).setText("Пол");
        table.getRow(counter).getCell(1).setText(user.getGender());

        // Добавляем изображение
        byte[] imageBytes = user.getPhoto();
        if (imageBytes != null && imageBytes.length > 0) {
            try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                String imageType = getImageType(imageBytes);

                if (imageType != null) {
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    if (bufferedImage != null) {
                        int imageWidth = bufferedImage.getWidth();
                        int imageHeight = bufferedImage.getHeight();

                        XWPFParagraph imageParagraph = document.createParagraph();
                        imageParagraph.setAlignment(ParagraphAlignment.CENTER);
                        XWPFRun imageRun = imageParagraph.createRun();

                        int poiImageType = getPoiImageType(imageType);
                        imageRun.addPicture(inputStream, poiImageType, "image", Units.pixelToEMU(500), Units.pixelToEMU(500));
                    } else {
                        System.out.println("Не удалось прочитать изображение.");
                    }
                } else {
                    System.out.println("Не удалось определить тип изображения.");
                }
            }
        } else {
            System.out.println("Изображение отсутствует или пустое.");
        }

        try (FileOutputStream out = new FileOutputStream(fileName)) {
            document.write(out);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        document.write(byteArrayOutputStream);
        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    public void deleteDocxFile() {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private int getRows() {
        if (user.getMiddleName() != null) {
            return 5;
        }

        return 4;
    }

    private static String getImageType(byte[] imageBytes) {
        if (imageBytes.length >= 2) {
            if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
                return "JPEG";
            } else if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50) {
                return "PNG";
            } else if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49) {
                return "GIF";
            } else if (imageBytes[0] == (byte) 0x42 && imageBytes[1] == (byte) 0x4D) {
                return "BMP";
            }
        }
        return null;
    }

    private static int getPoiImageType(String imageType) {
        switch (imageType) {
            case "JPEG":
                return XWPFDocument.PICTURE_TYPE_JPEG;
            case "PNG":
                return XWPFDocument.PICTURE_TYPE_PNG;
            case "GIF":
                return XWPFDocument.PICTURE_TYPE_GIF;
            case "BMP":
                return XWPFDocument.PICTURE_TYPE_BMP;
            default:
                throw new IllegalArgumentException("Неподдерживаемый тип изображения: " + imageType);
        }
    }
}
