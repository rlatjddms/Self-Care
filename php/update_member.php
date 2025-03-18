<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

if ($con->connect_error) {
    die("Connection failed: " . $con->connect_error);
}

$columnName = $_POST['columnName'];
$userId = $_POST['userId'];
$item = $_POST['item'];

$sql = "UPDATE member SET $columnName = ? WHERE id = ?";
$stmt = $con->prepare($sql);
$stmt->bind_param("ss", $item, $userId);

if ($stmt->execute()) {
    echo "Record updated successfully";
} else {
    echo "Error updating record: " . $con->error;
}

$stmt->close();
$con->close();
?>
