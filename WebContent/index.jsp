<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="styles/style.css" />
  <title>CS5300 Project 1</title>
</head>
<body>
  <h2><%= request.getAttribute("message") %></h2>
  <form name="SelectAction" method="get" action="MainServlet">
    <input type="submit" name="command" value="Replace">
    <input type="text" name="replaceText" size=40 maxlength=512><br />
    <input type="submit" name="command" value="Refresh"><br />
    <input type="submit" name="command" value="LogOut">
  </form>
  <h3>Server ID executing the client request: <%= request.getAttribute("serverAddr") + ":" + request.getAttribute("serverPort") %></h3>
  <h3>Discard Time: <%= request.getAttribute("Discard_Time") %></h3>
  <h3>Expiration Time: <%= request.getAttribute("Expires") %></h3>
  <h3>Version Number: <%= request.getAttribute("vNum") %></h3>
  <h3>IPP Primary: <%= request.getAttribute("IPPPrimary") %></h3>
</body>
</html>