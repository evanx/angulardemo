

function helloWorld() {
    return "Hello Jasmine!";
}

function inject() {
}

describe("Jasmine", function() {

    it("says hello", function() {
        expect(helloWorld()).toEqual("Hello Jasmine!");
    });
})

describe("App", function() {

    beforeEach(function() {
        angular.mock.module(['app'])
    });

    it('should have appService', inject(['appService', function(appService) {
    }]));    
});