<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

$id = isset($_POST["id"]) ? $_POST["id"] : "";
$pw = isset($_POST["pw"]) ? $_POST["pw"] : "";

$statement = mysqli_prepare($con, "SELECT * FROM member WHERE id = ? AND pw = ?");
if (!$statement) {
    die('mysqli_prepare() failed: ' . mysqli_error($con));
}

mysqli_stmt_bind_param($statement, "ss", $id, $pw);

if (!mysqli_stmt_execute($statement)) {
    die('mysqli_stmt_execute() failed: ' . mysqli_stmt_error($statement));
}

mysqli_stmt_store_result($statement);

mysqli_stmt_bind_result($statement, $idResult, $pwResult, $name, $gender, $birth, $phone, $email, $address, $recentSearch, $medicalHistory, $bookmark);

$response = array();
$response["success"] = false;

if (mysqli_stmt_fetch($statement)) {
    $response["success"] = true;
    $response["id"] = $idResult;
    $response["pw"] = $pwResult;
    $response["name"] = $name;
    $response["gender"] = $gender;
    $response["birth"] = $birth;
    $response["phone"] = $phone;
    $response["email"] = $email;
    $response["address"] = $address;
    $member["recentSearch"] = $recentSearch;
    $member["medicalHistory"] = $medicalHistory;
    $member["bookmark"] = $bookmark;
}

echo json_encode($response);
?>
