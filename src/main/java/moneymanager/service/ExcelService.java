package moneymanager.service;

import jakarta.mail.MessagingException;
import moneymanager.dto.ExpenseDTO;
import moneymanager.dto.IncomeDTO;
import moneymanager.entity.IncomeEntity;
import moneymanager.entity.ProfileEntity;
import moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final EmailService emailService;

    public void writeIncomesToExcel(OutputStream os, List<IncomeDTO> incomes) throws IOException{
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Incomes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("S.No");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Amount");
            header.createCell(4).setCellValue("Date");

            IntStream.range(0, incomes.size())
                    .forEach(i -> {
                        IncomeDTO income = incomes.get(i);
                        Row row = sheet.createRow(i + 1);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(income.getName() != null ? income.getName() : "N/A");
                        row.createCell(2).setCellValue(income.getCategoryId() != null ? income.getCategoryName() : "N/A");
                        row.createCell(3).setCellValue(income.getAmount() != null ? income.getAmount().doubleValue() : 0);
                        row.createCell(4).setCellValue(income.getDate() != null ? income.getDate().toString() : "N/A");
                    });

            workbook.write(os);
        }
    }

    public void writeExpensesToExcel(OutputStream os, List<ExpenseDTO> expenses) throws IOException{
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("S.No");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Amount");
            header.createCell(4).setCellValue("Date");

            IntStream.range(0, expenses.size())
                    .forEach(i -> {
                        ExpenseDTO expense = expenses.get(i);
                        Row row = sheet.createRow(i + 1);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(expense.getName() != null ? expense.getName() : "N/A");
                        row.createCell(2).setCellValue(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A");
                        row.createCell(3).setCellValue(expense.getAmount() != null ? expense.getAmount().doubleValue() : 0);
                        row.createCell(4).setCellValue(expense.getDate() != null ? expense.getDate().toString() : "N/A");
                    });

            workbook.write(os);
        }
    }

    public byte[] generateIncomeExcel() {

        ProfileEntity profile = profileService.getCurrentProfile();

        List<IncomeEntity> incomes = incomeRepository.findByProfileIdOrderByDateDesc(profile.getId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Income Details");

            // Create header row
            Row headerRow = sheet.createRow(0);

            String[] headers = {
                    "Income ID",
                    "Income Name",
                    "Amount",
                    "Date",
                    "Icon",
                    "Category"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add income data
            int rowIndex = 1;

            for (IncomeEntity income : incomes) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(income.getId());
                row.createCell(1).setCellValue(income.getName());

                if (income.getAmount() != null) {
                    row.createCell(2).setCellValue(income.getAmount().doubleValue());
                } else {
                    row.createCell(2).setCellValue(0);
                }

                if (income.getDate() != null) {
                    row.createCell(3).setCellValue(income.getDate().toString());
                }

                row.createCell(4).setCellValue(income.getIcon() != null ? income.getIcon() : "");

                row.createCell(5).setCellValue(income.getCategory() != null ? income.getCategory().getName() : "");
            }

            // Adjust column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate income Excel file", e);
        }
    }

    public void emailIncomeExcel() throws MessagingException {

        ProfileEntity profile = profileService.getCurrentProfile();

        byte[] excelFile = generateIncomeExcel();

        emailService.sendIncomeExcelEmail(profile.getEmail(), excelFile);
    }
}