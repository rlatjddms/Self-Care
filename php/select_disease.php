<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM disease";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    $member["diseaseName"] = $row["diseaseName"];
    $member["diagnosis"] = $row["diagnosis"];
    $member["definition"] = $row["definition"];
    $member["symptom"] = $row["symptom"];
    $member["cause"] = $row["cause"];
    $member["management"] = $row["management"];
    $member["medicalSubject"] = $row["medicalSubject"];
    $member["category"] = $row["category"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["disease"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
