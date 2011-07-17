<!--
  #%L
  Webmotion in test
  
  $Id$
  $HeadURL$
  %%
  Copyright (C) 2011 Debux
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 3 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  -->
<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>

    <head>
        <meta charset="utf-8">
        <c:if test="${requestScope.action == 'display'}">
            <title>Site map</title>
        </c:if>
        <c:if test="${requestScope.action == 'media'}">
            <title>Media map</title>
        </c:if>
        <link rel="icon" type="image/png" href="<c:url value="/img/collaboration.png"/>" />
                
        <script type="text/javascript" src="<c:url value="/js/prototype.js"/>"></script>
        
        <link rel="stylesheet" href="<c:url value="/css/classic.css"/>" type="text/css"  media="screen">
    </head>

    <body>

        <div id="header">
            <fmt:setBundle basename="config"/>
            <div class="logo"><fmt:message key="site.name"/></div>
            <div class="nav">
                <jsp:include page="/deploy/include/menu_header" />
            </div>
        </div>

        <div id="main">
            <div id="main_content">
                <c:if test="${requestScope.action == 'display'}">
                    <h1>Site map</h1>
                </c:if>
                <c:if test="${requestScope.action == 'media'}">
                    <h1>Media map</h1>
                </c:if>
                <ul>
                <c:forEach var="map" items="${requestScope.map}" >
                    <c:if test="${map.key != null}">
                        <li>${map.key}</li>
                        <ul>
                        <c:forEach var="page" items="${map.value}" >
                            <li><a href="<c:url value="/deploy/${requestScope.action}/${map.key}/${page}"/>">${page}</a></li>
                        </c:forEach>
                        </ul>
                    </c:if>
                    <c:if test="${map.key == null}">
                        <c:forEach var="page" items="${map.value}" >
                            <li><a href="<c:url value="/deploy/${requestScope.action}/${page}"/>">${page}</a></li>
                        </c:forEach>
                    </c:if>
                </c:forEach>
                </ul>
            </div>
        </div>

        <div id="footer">
            <div class="nav">
                <jsp:include page="/deploy/include/menu_footer" />
            </div>
            <div>Powerd by WikiMotion and WebMotion</div>
        </div>

    </body>

</html>
