

var app = angular.module("app", ["ngSanitize"]);

app.controller("glamourController", ["$scope",
    function($scope) {
        $scope.gq = {
            title: "GQ",
            link: "http://gq.co.za",
            logoImage: "",
            entries: [
                {
                    title: "Johnnie Walker: Meet The New Kings Of Flavour",
                    lead: "In case you haven't already met them in the glossy pages of GQ, it's our pleasure to introduce you to Johnnie Walker's new Kings (and one Queen) of Flavour. ",
                    link: "http://gq.co.za/2014/04/johnnie-walker-introducing-new-kings-flavour"
                },
                {
                    title: "London Collections: Men Preview – Diego Vanassibara",
                    lead: "Diego Vanassibara – Men’s Shoe Designer, London “Give men the opportunity to dream, too” - That’s one of the main motivations behind the Diego Vanassibara brand",
                    link: "http://gq.co.za/2014/04/london-collections-men-preview-diego-vanassibara/"
                },
                {
                    title: "Personal Style Diary: Ofentse Lephoi",
                    lead: "Ofentse gives us an example of outdoor effortless chic Day 9: Shirt: Designed By Ofentse Shorts: Markham Loafers: PQ Though Ofentse Lephoi decided to study electrical engineering, he was enthusiastic about style long before he realised what his vocation in life would be.",
                    link: "http://gq.co.za/2014/04/personal-style-diary-ofentse-lephoi-9/"
                },
                {
                    title: "How To: The Square Fold Pocket Square",
                    lead: "Every gentleman needs to master the art of folding pocket squares. We’ve made your life easier by breaking it down into four basic types with instructions and descriptions of when and how to wear them.",
                    link: "http://gq.co.za/2014/04/square-fold-pocket-square-how-to"
                },
                {
                    title: "Introducing JD by Shaldon Kopman",
                    lead: "You know him as the inimitable stylist-turned-menswear designer, famous for his impeccable tailoring skills, his incredible understanding of style, and for all those mind-boggling details that his collections have become internationally renowned for.",
                    link: "http://gq.co.za/2014/04/introducing-jd-shaldon-kopman/"
                }
            ]
        };
        $scope.glamour = {
            title: "Glamour",
            link: "http://glamour.co.za",
            logoImage: "",
            entries: [
                {
                    title: "Drake and Rihanna are getting serious",
                    lead: "",
                    link: "http://glamour.co.za/2014/04/drake-rihanna-getting-serious/",
                    image: "http://glamour.co.za/wp-content/uploads/2014/04/MG_8201.jpg"
                },
                {
                    title: "Olivia Wilde welcomes a baby boy",
                    lead: "",
                    link: "http://glamour.co.za/2014/04/olivia-wilde-welcomes-baby-boy/"
                },
                {
                    title: "John Legend thinks he'll be a good dad",
                    lead: "",
                    link: "http://glamour.co.za/2014/04/john-legend-thinks-hell-good-dad/"
                },
                {
                    title: "",
                    lead: "",
                    link: ""
                },
                {
                    title: "",
                    lead: "",
                    link: ""
                },
                {
                    title: "",
                    lead: "",
                    link: ""
                }
            ]
        };
    }]);

