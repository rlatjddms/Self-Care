<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM hospital";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    $member["no"] = $row["no"];
    $member["hospitalName"] = $row["hospitalName"];
    $member["medicalSubject"] = $row["medicalSubject"];
    $member["address"] = $row["address"];
    $member["phone"] = $row["phone"];
    $member["url"] = $row["url"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["hospital"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
