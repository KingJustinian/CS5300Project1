<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CS5300 Project 1</title>
</head>
<body>
  <h1>Hello, User!</h1>
  <form name="replace" method="get" action="MainServlet">
    <input type="submit" name="command" value="Replace">
    <input type="text" name="replaceText" size=40 maxlength=512>
  </form>
</body>
</html>