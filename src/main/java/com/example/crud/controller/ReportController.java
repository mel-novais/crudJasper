package com.example.crud.controller;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<byte[]> generateReport() {
        try {
            InputStream reportStream = getClass().getResourceAsStream("/reports/relatorioProduto.jrxml");

            if (reportStream == null) {
                return ResponseEntity.notFound().build();
            }
            List list = jdbcTemplate.queryForList("SELECT id,\n" +
                    "\tname,\n" +
                    "\tprice,\n" +
                    "\tactive\n" +
                    "FROM crud_db.product");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), DataSourceUtils.getConnection(dataSource));
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), jdbcTemplate.getDataSource());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(),
                    new JRBeanCollectionDataSource((Collection) list));

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename("relatorio.pdf").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
