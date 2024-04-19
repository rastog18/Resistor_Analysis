<?php
	// Parse parameter inputs
	$image = $_POST["image"];
	$band_num = $_POST["bandNum"];

	// Make sure to create resistor.jpg and give it rw access: chmod +rw resistor.jpg
	// Add image to file to be read by calculate.py
	$handle = file_put_contents("resistor.jpg", base64_decode($image));
	
	if($handle) {
		// Create shell cmd using myvenv's python
		$pyece = escapeshellcmd("/data/jjhuang/anaconda3/envs/plantenv/bin/python calculate.py " . $band_num);
      	$output = shell_exec($pyece);
      	echo $output;
  	}
	else {
		echo "Image Failed to Upload";
	}
?>

