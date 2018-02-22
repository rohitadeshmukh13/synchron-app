angular.module('SynchronApp', [])

.controller('sensorDataController', function($scope, $http) {
	$scope.formData = {};
	$scope.displaySensorData = false;
	
	$scope.formData.sensor = "http://www.agtinternational.com/ontologies/WeidmullerMetadata#Machine_59";
	$scope.formData.timestamp1 = "2016-01-01T01:00:01+01:00";
	$scope.formData.timestamp2 = "2018-01-01T01:00:01+01:00";
	
	// when landing on the page, get all sensors and show them in a list
	$http.get('/getSensors')
	.success(function(data) {
		$scope.sensorsList = data;
		console.log(data);
	})
	.error(function(data) {
		console.log('Error: ' + data);
	});

	// query the sensor data
	$scope.getSensorData = function() {
		//$scope.formData.timestamp1 = '\"' + $scope.formData.timestamp1 + '\"^^xsd:dateTime';
		//$scope.formData.timestamp2 = '\"' + $scope.formData.timestamp2 + '\"^^xsd:dateTime';

		$http.post('/getSensorData', $scope.formData)
		.success(function(data) {
			//$scope.formData = {};
			$scope.sensorObservations = data;
			$scope.displaySensorData = true;
			console.log(data);
		})
		.error(function(data) {
			console.log('Error: ' + data);
		});
	};

});
