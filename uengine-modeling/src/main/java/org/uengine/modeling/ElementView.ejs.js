var org_uengine_modeling_ElementView_disable_clonning = false;

var org_uengine_modeling_ElementView = function (objectId, className) {
    this.objectId = objectId;
    this.className = className;
    this.object = mw3.objects[this.objectId];

    //if(!this.object.element) {throw new Error("No element data for ElementView.");}

    if(this.object.element)
        this.__className = this.object.element.__className;

    this.objectDivId = mw3._getObjectDivId(this.objectId);
    this.objectDiv = $(document.getElementById(this.objectDivId));
    if (!this.object) return true;

    this.isNew = true;
    this.metadata = mw3.getMetadata(this.className);


    this.getValue = function () {
        if (this.element) {
            if ($('#' + this.element.id).length == 0)
                return {__objectId: this.objectId, __className: this.className};

            this.element = document.getElementById(this.element.id);
            this.object.label = this.element.shape.label;
            this.object.x = this.element.shape.geom.getBoundary().getCentroid().x;
            this.object.y = this.element.shape.geom.getBoundary().getCentroid().y;
            this.object.width = this.element.shape.geom.getBoundary().getWidth();
            this.object.height = this.element.shape.geom.getBoundary().getHeight();
            this.object.id = this.element.id;
            this.object.shapeId = this.element.shape.SHAPE_ID;
            this.object.style = escape(OG.JSON.encode(this.element.shapeStyle));
            this.object.toEdge = $(this.element).attr('_toedge');
            this.object.fromEdge = $(this.element).attr('_fromedge');
            this.object.index = $(this.element).prevAll().length;
            if ($(this.element).parent().attr('id') === $(this.rootgroup).attr('id')) {
                this.object.parent = 'null';
            } else {
                this.object.parent = $(this.element).parent().attr('id');
            }
            return this.object;
        }
    };


    this.getLabel = function () {
        if (!this.object.label && this.object.element && this.object.element.name)
            this.object.label = this.object.element.name;
        //mw3.getObjectNameValue(this.object.element, true);

        return unescape(this.object.label ? this.object.label : '');
    };

    this.getCanvas = function () {
        var canvasId = mw3.getClosestObject(this.objectId, "org.uengine.modeling.Canvas").__objectId;
        var object = mw3.objects[canvasId];
        return object.canvas;
    };

    this.getRenderer = function () {
        return this.getCanvas()._RENDERER;
    };

    this.setParent = function (elementId, parentId) {
        if (!elementId || !parentId) {
            return;
        }
        if (!this.canvas.groupReservations) {
            this.canvas.groupReservations = {};
        }
        var element = this.renderer.getElementById(elementId);
        var parentElement = this.renderer.getElementById(parentId);

        if (!element) {
            return;
        }

        //�?모�? 캔버?��?�� ?���? 그려�?�? ?��?��?�� 경우 ?��?��?�� 걸어?��?��?��.
        if (!parentElement) {
            if (!this.canvas.groupReservations[parentId]) {
                this.canvas.groupReservations[parentId] = [];
            }
            if (this.canvas.groupReservations[parentId].indexOf(elementId) === -1) {
                this.canvas.groupReservations[parentId].push(elementId);
            }
        }

        //?��?��?���? ?��?��?�� 걸려?��?��것이 ?��?���? 그룹?�� 맺는?��.
        var reservations = this.canvas.groupReservations[elementId];
        if (!reservations) {
            return;
        }

        for (var i = 0; i < reservations.length; i++) {
            var reservedElementId = reservations[i];
            var reservedElement = this.renderer.getElementById(reservedElementId);
            if (reservedElement) {
                element.appendChild(reservedElement);
            }
        }
    };

    this.init = function () {

        //2번을 ?��.

        this.canvas = this.getCanvas();
        this.renderer = this.getRenderer();
        this.element = null;
        this.rootgroup = this.renderer.getRootGroup();
        this.byDrop = this.object.byDrop;

        //verification data first.
        if (this.object.shapeId == null)
            throw new Error("No shape Id is set for " + this.object);


        //concern �? ?��?�� ?��?��?�� ?��?�� by soo
        //?�� �?분과
        var concern, concernColor, lineColor;
        if (this.object && this.object.element && this.object.element.concern != null) {
            concern = this.object.element.concern;

            if (concern == "Customer") {
                concernColor = "#2ecc71 ";
                lineColor = "#27ae60 ";
            }
            else if (concern == "Solution") {
                concernColor = "#f1c40f ";
                lineColor = "#f39c12 ";
            }
            else if (concern == "Endeavor") {
                concernColor = "#3498db ";
                lineColor = "#2980b9 ";
            }
        }


        var existElement = this.canvas.getElementById(this.object.id);
        if (existElement) {
            this.canvas.drawLabel(existElement, this.getLabel());
            this.element = existElement;
            this.isNew = false;

            //concern �? color ?��?�� by soo
            //?�� �?�?
            if (concern != null)
                this.canvas._RENDERER.setShapeStyle(this.element, {
                    "fill": concernColor,
                    "fill-opacity": 1,
                    "stroke": lineColor
                });

        } else {

            var shape = eval('new ' + this.object.shapeId);
            shape.label = this.getLabel();

            if(org_uengine_modeling_ElementView_disable_clonning){
                shape.CONNECT_CLONEABLE = false;
            }

            if(shape.TaskType == 'None'){
                shape.TaskType = 'AUTO';
            }

            if (this.object.instStatus) {
                //  if ("Completed" == this.object.instStatus || "Running" == this.object.instStatus) {
                shape.status = this.object.instStatus;
                // }
            }

            var style = this.object.style;
            var boundary;

            //concern �? color ?��?�� by soo
            //?�� �?분을 추�??��?���? ?�� �? 같습?��?��.
            if (style == "null" && concern != null) {
                style = new OG.geometry.Style({
                    fill: concernColor,
                    "fill-opacity": 1,
                    'stroke': lineColor
                });
            }

            var preventDrop = this.byDrop ? false : true;

            //3�?. ?��?��?��?���? 캔버?��?�� 그림.
            this.element = this.canvas.drawShape([this.object.x, this.object.y],
                shape,
                [parseInt(this.object.width, 10), parseInt(this.object.height, 10)],
                OG.JSON.decode(unescape(style)),
                this.object.id,
                this.object.parent,
                preventDrop,
                this.__className);

            //5�?. 그리?���? ?��료되?��?��.

            this.setParent(this.element.id, this.object.parent);
            if (this.renderer.isLane(this.element)) {
                this.renderer.fitLaneOrder(this.element);
            }

            boundary = this.element.shape.geom.boundary;

            this.autoResizeCanvas(boundary);

            this.object[this.metadata.keyFieldDescriptor.name] = this.element.id;

            mw3.putObjectIdKeyMapping(this.objectId, this.object, true);
        }

        if (this.object.toEdge) {
            $(this.element).attr('_toedge', this.object.toEdge);
        }

        if (this.object.fromEdge) {
            $(this.element).attr('_fromedge', this.object.fromEdge);
        }

        boundary = this.element.shape.geom.boundary;

        this.autoResizeCanvas(boundary);

        this.object[this.metadata.keyFieldDescriptor.name] = this.element.id;

        mw3.putObjectIdKeyMapping(this.objectId, this.object, true);

        $(this.element).trigger('loaded.' + this.element.id);

        //캔버?���? 리모?�� 모드?���? ?��로퍼?�� �?경으�? ?��?�� ?���? 그려�? ?��리먼?��?��경우 브로?��캐스?�� ?��?��
        if (this.canvas.getRemotable()) {
            if (this.object.changed) {
                OG.RemoteHandler.broadCastCanvas(this.canvas, function (canvas) {

                });
            }
        }

        this.bindMapping();
    };

    this.bindMapping = function () {
        var metadata = mw3.getMetadata(this.className);
        var contextMenus = [];
        for (var methodName in metadata.serviceMethodContextMap) {
            if (mw3.isHiddenMethodContext(this.metadata.serviceMethodContextMap[methodName], this.object))
                continue;

            var methodContext = metadata.serviceMethodContextMap[methodName];

            if (methodContext.eventBinding) {
                for (var eventNameIndex in methodContext.eventBinding) {
                    var eventName = methodContext.eventBinding[eventNameIndex];

                    this.bind(eventName);
                }
            }

            if (methodContext.mouseBinding) {
                var which = 3;
                if (methodContext.mouseBinding == "right")
                    which = 3;
                else if (methodContext.mouseBinding == "left")
                    which = 1;

                if (methodContext.mouseBinding == "drop") {

                    $(this.element).droppable({
                        greedy: true,
                        tolerance: 'geom'
                    }).attr('droppable', true);

                    var command = "if(mw3.objects['" + this.objectId + "']!=null) mw3.call(" + this.objectId + ", '" + methodName + "')";
                    $(this.element).on('drop.' + this.objectId, {command: command}, function (event, ui) {
                        if (Object.prototype.toString.call(ui.draggable[0]) != "[object SVGGElement]") {
                            if (Object.prototype.toString.call(ui.draggable[0]) != "[object SVGRectElement]") {
                                eval(event.data.command);
                            }
                        }
                    });
                } else {
                    // click(mouse right) is contextmenu block
                    if (which == 3) {

                        $(this.element).bind('contextmenu', function (event) {
                            return false;
                        });
                    }

                    $(this.element).on((which == 3 ? 'mouseup' : 'click') + '.' + this.objectId, {
                        which: which,
                        objectId: this.objectId
                    }, function (event) {
                        $(document.getElementById(mw3._getObjectDivId(event.data.objectId))).trigger(event);
                    });

                }
            }

            if(methodContext.inContextMenu){
                contextMenus[contextMenus.length] = methodContext;
            }
        }

        // implements the context menu events
        var items = {}; var touched = false;
        for(var i in contextMenus){
            var contextMenuMethod = contextMenus[i];

            var command = "mw3.objects[" + this.objectId + "]." + contextMenuMethod.methodName + "()"
            console.log("command="+command);

            items[command] =
            {
                name: contextMenuMethod.displayName ? contextMenuMethod.displayName : contextMenuMethod.methodName
            };

            touched = true;
        }

        if(touched){
            $.contextMenu( 'destroy', "#" + this.element.id );
            $.contextMenu({
                position: function (opt, x, y) {
                    opt.$menu.css({top: y + 10, left: x + 10});
                },
                selector: "#" + this.element.id,
                callback: function (key, options) {
                    console.log("key="+key);
                    eval(key);

                },
                items: items
            });

            console.log('touched')
        }
    }




    this.bind = function (name) {
        try{

            var events = $(this.element).data("events")[name];

            for(var i in events){
                var event = events[i];
                if(event.namespace == this.objectId)
                    return; //already existing event
            }

        }catch(e){}

        $(this.element).bind(name + '.' + this.objectId, {objectId: this.objectId}, function (event, ui) {
            $(document.getElementById(mw3._getObjectDivId(event.data.objectId))).trigger(event.type);
            event.stopPropagation();
        });
    };

    this.destroy = function () {
        if ($(this.element).attr('droppable')) {
            try {
                $(this.element).droppable("destroy");
            } catch(e){}

        }

        $(this.element).unbind('.' + this.objectId);
    };

    this.autoResizeCanvas = function (boundary) {
        var rootBBox = this.canvas._RENDERER.getRootBBox();
        if (rootBBox.width < (boundary._centroid.x + boundary._width) * this.canvas._CONFIG.SCALE) {
            this.canvas._RENDERER.setCanvasSize([boundary._centroid.x + boundary._width, rootBBox.height]);
        }
        if (rootBBox.height < (boundary._centroid.y + boundary._height) * this.canvas._CONFIG.SCALE) {
            this.canvas._RENDERER.setCanvasSize([rootBBox.width, boundary._centroid.y + boundary._height]);
        }
    };

    this.init();
};


OG.renderer.RaphaelRenderer.prototype.drawStatus = function (element) {
    var me = this, rElement = this._getREleById(OG.Util.isElement(element) ? element.id : element),
        geometry = rElement ? rElement.node.shape.geom : null,
        envelope, _upperLeft, _bBoxRect, _rect, _rect1,
        _size = me._CONFIG.COLLAPSE_SIZE,
        _hSize = _size / 2;

    _rect1 = this._getREleById(rElement.id + OG.Constants.STATUS_SUFFIX); //shape itself
    if (_rect1) {
        this._remove(_rect1);
    }

    _rect = this._getREleById(rElement.id + OG.Constants.STATUS_SUFFIX + '_IMG');
    if (_rect) {
        this._remove(_rect);
    }

    envelope = geometry.getBoundary();
    _upperRight = envelope.getUpperRight();

    switch(element.shape.status){
        case "Completed":
            _rect1 = this._PAPER.image("images/opengraph/complete.png", _upperRight.x - 25, _upperRight.y  + 5, 20, 20);
            break;
        case "Running":
            _rect = this._PAPER.rect(envelope.getUpperLeft().x - 10, envelope.getUpperLeft().y - 10, envelope.getWidth() + 20, envelope.getHeight() + 20);
            _rect.attr("fill", "#C9E2FC");
            _rect.attr("stroke-width", "0.2");
            _rect.attr("r", "10");
            _rect.attr("fill-opacity", "1");
            _rect.attr("stroke-dasharray", "--");

            _rect1 = this._PAPER.image("images/opengraph/running.png", _upperRight.x - 25, _upperRight.y  + 5, 20, 20);
            break;
        case "Suspended":
            _rect1 = this._PAPER.image("images/opengraph/pause.png", _upperRight.x - 25, _upperRight.y  + 5, 20, 20);
            break;
        case "Multiple":
            _rect1 = this._PAPER.image("images/opengraph/parallel.png", _upperRight.x - 25, _upperRight.y  + 5, 20, 20);
            break;
        case "Failed":
            _rect1 = this._PAPER.image("images/opengraph/fault.png", _upperRight.x - 25, _upperRight.y  + 5, 20, 20);
            break;
        case "Stopped":
            _rect1 = this._PAPER.image("images/opengraph/stop.png", _upperRight.x - 25, _upperRight.y  + 5, 20, 20);
            break;
    }

    if(element.shape.status == "Running"){
        var ani1 = Raphael.animation({
            fill:'#C9E2FC'
        },1000);

        var ani2 = Raphael.animation({
            fill:'white'
        },1000, startAni);

        function startAni(){
            _rect.attr({fill: 'white'}).animate(ani1);
            _rect.attr({fill: '#C9E2FC'}).animate(ani2.delay(1000));
        }
        startAni();
    }

    if(_rect1) {  // _rect1 is badge or marker shape. if null don't draw it.
        this._add(_rect1, rElement.id + OG.Constants.STATUS_SUFFIX);
        _rect1.insertAfter(rElement);
        rElement.appendChild(_rect1);
    }

    if(_rect){
        this._add(_rect, rElement.id + OG.Constants.STATUS_SUFFIX + '_IMG');
        _rect.insertAfter(rElement);
        rElement.prependChild(_rect);
    }

    return null;
};
