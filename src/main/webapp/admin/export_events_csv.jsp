<%@ page contentType="text/csv"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><% response.setHeader("Content-Disposition","attachment; filename=\"EPS_Events.csv\"");%>"Date","Time","Username","Password","Event","Tamperstamp Status"<c:set var="delimiter" value="${requestScope.delimiter}" /><c:forEach var="daysEvents" items="${requestScope.events}"><c:forEach var="thisEvent" items="${daysEvents.events}"><c:choose><c:when test="${thisEvent.tamperstampStatus == -1}"><c:set var="tamperstamp" value="Unavailable"/></c:when><c:when test="${thisEvent.tamperstampStatus == 0}"><c:set var="tamperstamp" value="Untampered"/></c:when><c:otherwise><c:set var="tamperstamp" value="Tampered"/></c:otherwise></c:choose>
<fmt:formatDate pattern="dd-MMM-yyyy" value="${thisEvent.date}" />${delimiter}<fmt:formatDate pattern="HH:mm:ss" value="${thisEvent.date}" />${delimiter}<c:choose><c:when test="${thisEvent.username == null}">" "</c:when><c:otherwise>${thisEvent.username}</c:otherwise></c:choose>${delimiter}${thisEvent.item}${delimiter}${thisEvent.humanReadableMessage}${delimiter}${tamperstamp}</c:forEach></c:forEach>
