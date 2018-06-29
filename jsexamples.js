var p = $('p');

p
 .html(function(index, oldHtml) {
    return oldHtml.replace(/\b(\w+?)\b/g, '<span class="word">$1</span>')
 })
 $('body').click(function(event) {console.log(event) });



document.addEventListener('click', function(e) {
    e = e || window.event;
    var target = e.target || e.srcElement,
        text = target.textContent || text.innerText;   
console.log(text);console.log(e)
return false;
}, false);


document.addEventListener('click', function(e) {
e.preventDefault();  
var s = window.getSelection();
        s.modify('extend','backward','word');        
        var b = s.toString();

        s.modify('extend','forward','word');
        var a = s.toString();
        s.modify('move','forward','character');
        console.log(b+a),console.log(e);
}, false);



document.addEventListener('click', function(e) {
e.preventDefault();
         s = window.getSelection();
         var range = s.getRangeAt(0);
         var node = s.anchorNode;
         while(range.toString().indexOf(' ') != 0) {                 
            range.setStart(node,(range.startOffset -1));
         }
         range.setStart(node, range.startOffset +1);
         do{
           range.setEnd(node,range.endOffset + 1);

        }while(range.toString().indexOf(' ') == -1 && range.toString().trim() != '');
        var str = range.toString().trim();
        alert(str);
}, false);



javascript: document.addEventListener('click', function(e) {
       if( injectedObject.isEnable ()){
            e.preventDefault();
            var target = e.target || e.srcElement;
            var text;    
            
            if (target.tagName=='A'){
                text = target.textContent || text.innerText;         
            }else{
            
                var s = window.getSelection();
                s.modify('extend','backward','word');        
                var b = s.toString();

                s.modify('extend','forward','word');
                var a = s.toString();
                s.modify('move','forward','character');
                    
                text=b+a;
            }
            injectedObject.translate(text);
       }         
}, false);


javascript:document.onselectionchange = function () {var t=window.getSelection().toString(); if (t!='') {  injectedObject.translate (t)} } 
