<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM question";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    $member["no"] = $row["no"];
    $member["question"] = $row["question"];
    $member["answer"] = $row["answer"];
    $member["result"] = $row["result"];
    $member["next"] = $row["next"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["question"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
