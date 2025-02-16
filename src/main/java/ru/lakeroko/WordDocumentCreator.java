package ru.lakeroko;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class WordDocumentCreator {
    public static void main(String[] args) throws IOException {
        // Создаем новый документ
        XWPFDocument document = new XWPFDocument();

        // Создаем заголовок
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("Отчет о проделанной работе");
        titleRun.setBold(true);
        titleRun.setFontSize(20);

        // Создаем параграф с текстом
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setText("Данный отчет содержит информацию о проделанной работе за последний месяц.");
        paragraphRun.setFontSize(14);

        // Создаем таблицу
        XWPFTable table = document.createTable(4, 3);

        // Заполняем заголовки таблицы
        table.getRow(0).getCell(0).setText("№");
        table.getRow(0).getCell(1).setText("Задача");
        table.getRow(0).getCell(2).setText("Статус");

        // Заполняем данные в таблице
        table.getRow(1).getCell(0).setText("1");
        table.getRow(1).getCell(1).setText("Разработка модуля авторизации");
        table.getRow(1).getCell(2).setText("Завершено");

        table.getRow(2).getCell(0).setText("2");
        table.getRow(2).getCell(1).setText("Тестирование API");
        table.getRow(2).getCell(2).setText("В процессе");

        table.getRow(3).getCell(0).setText("3");
        table.getRow(3).getCell(1).setText("Документирование кода");
        table.getRow(3).getCell(2).setText("Не начато");

        // Сохраняем документ
        try (FileOutputStream out = new FileOutputStream("Report.docx")) {
            document.write(out);
        }

        System.out.println("Документ успешно создан!");
    }
}
