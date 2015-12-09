package org.smigo.log;

/*
 * #%L
 * Smigo
 * %%
 * Copyright (C) 2015 Christian Nilsson
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.inject.internal.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smigo.user.MailHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class LogHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MailHandler mailHandler;
    @Autowired
    private LogDao logDao;


    public void log(HttpServletRequest request, HttpServletResponse response) {
        log.info(getRequestDump(request, response));
        logDao.log(LogBean.create(request, response));
    }

    public String getRequestDump(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder s = new StringBuilder("Logging request>");
        s.append(LogBean.create(request, response).toString());
        s.append(" Auth type:").append(request.getAuthType());
        s.append(" Principal:").append(request.getUserPrincipal());
        s.append(" Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            s.append(headerName).append("=").append(request.getHeader(headerName)).append(" - ");
        }

        final long start = (Long) request.getAttribute(VisitLogger.REQUEST_TIMER);
        s.append(" Request time elapsed:").append(System.nanoTime() - start).append("ns which is ").append((System.nanoTime() - start) / 1000000).append("ms");
        return s.toString();
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void backup() throws MessagingException {
        log.info("Running backing - start");
        logDao.backup();
        log.info("Running backing - complete");
    }

    @Scheduled(cron = "0 0 12 * * FRI")
    public void sendWeeklyReport() throws MessagingException {
        StringBuilder mail = new StringBuilder();
        mail.append("<html><body>");

        mail.append("<h1>Weekly report</h1>");

        mail.append("<table>");
        mail.append("<tr><td>Available processors</td><td>").append(Runtime.getRuntime().availableProcessors()).append("</td></tr>");
        mail.append("<tr><td>Free memory</td><td>").append(Runtime.getRuntime().freeMemory()).append("</td></tr>");
        mail.append("<tr><td>Max memory</td><td>").append(Runtime.getRuntime().maxMemory()).append("</td></tr>");
        mail.append("<tr><td>Total memory</td><td>").append(Runtime.getRuntime().totalMemory()).append("</td></tr>");
        mail.append("</table>");

        mail.append(getHtmlTable(logDao.getUserReport()));
        mail.append(getHtmlTable(logDao.getReferrerReport()));
        mail.append(getHtmlTable(logDao.getSpeciesReport()));
        mail.append(getHtmlTable(logDao.getVarietiesReport()));
        mail.append(getHtmlTable(logDao.getSpeciesTranslationReport()));
        mail.append(getHtmlTable(logDao.getActivityReport()));
        mail.append("</body></html>");
        mailHandler.sendAdminNotificationHtml("weekly report", mail.toString());
    }

    private String getHtmlTable(QueryReport queryReport) {
        final List<Map<String, Object>> tableRows = queryReport.getResult();
        StringBuilder ret = new StringBuilder();
        List<String> columnNames = Lists.newArrayList(tableRows.iterator().next().keySet());
        ret.append("<p>").append(queryReport.getSql()).append("</p>");
        ret.append("<table border='1'>");
        ret.append("<tr>");
        for (String tableHeader : columnNames) {
            ret.append("<th>").append(tableHeader).append("</th>");
        }
        ret.append("</tr>");
        for (Map<String, Object> row : tableRows) {
            ret.append("<tr>");
            for (String column : columnNames) {
                ret.append("<td nowrap>").append(row.get(column)).append("</td>");
            }
            ret.append("</tr>");
        }
        ret.append("</table>");
        return ret.toString();
    }

    public void logError(HttpServletRequest request, HttpServletResponse response, Exception exception, Integer statusCode, String uri) {
        if (uri != null) {
            request.setAttribute(VisitLogger.NOTE_ATTRIBUTE, uri);
        } else if (exception != null) {
            request.setAttribute(VisitLogger.NOTE_ATTRIBUTE, exception.getClass().getName() + ":" + exception.getMessage());
        }

        log.error("Error during request. (Outside Spring MVC) Statuscode:" + statusCode + " Uri:" + uri, exception);
        if (Objects.equals(statusCode, HttpStatus.NOT_FOUND.value())) {
            final String separator = System.lineSeparator();
            String text = "Statuscode:" + statusCode + separator + "Uri:" + uri + separator + getRequestDump(request, response) + separator;
            if (exception != null) {
                text = text + exception.getClass().getName() + exception.getMessage() + separator + Arrays.toString(exception.getStackTrace());
            }
            mailHandler.sendAdminNotification("error during request outside Spring MVC", text);
        }

    }
}
