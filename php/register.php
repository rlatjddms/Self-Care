<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

$id = isset($_POST["id"]) ? $_POST["id"] : "";
$pw = isset($_POST["pw"]) ? $_POST["pw"] : "";
$name = isset($_POST["name"]) ? $_POST["name"] : "";
$gender = isset($_POST["gender"]) ? $_POST["gender"] : "";
$birth = isset($_POST["birth"]) ? $_POST["birth"] : "";
$phone = isset($_POST["phone"]) ? $_POST["phone"] : "";
$email = isset($_POST["email"]) ? $_POST["email"] : "";
$address = isset($_POST["address"]) ? $_POST["address"] : "";
$recentSearch = isset($_POST["recentSearch"]) ? $_POST["recentSearch"] : "";
$medicalHistory = isset($_POST["medicalHistory"]) ? $_POST["medicalHistory"] : "";
$bookmark = isset($_POST["bookmark"]) ? $_POST["bookmark"] : "";

$statement = mysqli_prepare($con, "INSERT INTO member VALUES (?,?,?,?,?,?,?,?,?,?,?)");
mysqli_stmt_bind_param($statement, "sssssssssss", $id, $pw, $name, $gender, $birth, $phone, $email, $address, $recentSearch, $medicalHistory, $bookmark);
mysqli_stmt_execute($statement);

$response = array();
$response["success"] = true;

echo json_encode($response);
?>
