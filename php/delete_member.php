<?php
$con = mysqli_connect("localhost:3307", "root", "Calab307!!", "capstone");
mysqli_query($con, 'SET NAMES utf8');

// 연결 오류 확인
if (mysqli_connect_errno()) {
    die("Connection failed: " . mysqli_connect_error());
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $userId = $_POST['userId'];

    $sql = "DELETE FROM member WHERE id = ?";
    $stmt = mysqli_prepare($con, $sql);
    mysqli_stmt_bind_param($stmt, "s", $userId);

    if (mysqli_stmt_execute($stmt)) {
        echo "회원 삭제 성공";
    } else {
        echo "회원 삭제 실패: " . mysqli_stmt_error($stmt);
    }

    mysqli_stmt_close($stmt);
}

mysqli_close($con);
?>
