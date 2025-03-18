<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM member";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    $member["id"] = $row["id"];
    $member["pw"] = $row["pw"];
    $member["name"] = $row["name"];
    $member["gender"] = $row["gender"];
    $member["birth"] = $row["birth"];
    $member["phone"] = $row["phone"];
    $member["email"] = $row["email"];
    $member["address"] = $row["address"];
    $member["recentSearch"] = $row["recentSearch"];
    $member["medicalHistory"] = $row["medicalHistory"];
    $member["bookmark"] = $row["bookmark"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["member"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
