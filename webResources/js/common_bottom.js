// search box behavior
var searchElement = document.getElementById("searchInput");
if (searchElement) {
    var defaultSearchText = 'Search Met Office';
    // set the default text
    searchElement.value = defaultSearchText;

    // empty box on focus
    searchElement.onfocus = function() {
        if (this.value == defaultSearchText) this.value = '';
    };

    // repopulate empty box on blur
    searchElement.onblur = function() {
        if (this.value == '') this.value = defaultSearchText
    };
}
// make external links open in new window
if (document.getElementsByTagName){
    var anchors = document.getElementsByTagName("a");
    for (var i = 0; i < anchors.length; i++) {
        var anchor = anchors[i];
        if (anchor.getAttribute("href") && anchor.getAttribute("rel") == "external") anchor.target = "_blank";
    }
}