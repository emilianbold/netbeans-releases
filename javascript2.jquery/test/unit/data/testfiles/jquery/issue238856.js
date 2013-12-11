/**
 * BellyBestFriend weight HTML 5 Graph
 *
 * @author Mariusz Buk <mariusz@freelancer.katowice.pl>
 * @link http://freelancer.katowice.pl
 */
 
/**
 * required  this.options.
 * 'url' - graph json URL
 *
 * @param $ jQuery
 * @author Mariusz Buk
 */
 
(function ($) {
    "use strict";

    // constants
    var GRAPH_HEIGHT, trimester_array, margin;

    /**
     * Graph height
     */
    GRAPH_HEIGHT = 395;

    /**
     * Trimesters by week
     */
    trimester_array = [
        [1, 12],  // first
        [13, 26], // second
        [27, 42]  // third
    ];

    /**
     * Margins
     */
    margin = {
        bottom: 60,
        left: 60,
        right: 10, // 40
        top: 40,
        innerLeft: 20,
        innerBottom: 20,
        innerTop: 20,
        innerRight: 20
    };
   
    $.widget("ovu.weightGraph", {

         options: {

            /**
             * if graph is editable
             */
            editable: false,

            /**
             * Graph JSON URL
             */
            url: null,

            /**
             * Unit: metric, nonmetric
             */
            unit: null,

            /**
             * Current trimester
             */
            trimester: null,

            /**
             * Current week
             */
            week: null
        },

        /**
         * Internal functions
         */
        prv: {
            /**
             * Current widget object
             */
            widget: null,

            /**
             * JSON data
             */
            json: null,

            /**
             * Canvas
             */
            canvas: null,

            /**
             * Canvas context
             */
            context: null,

            /**
             * Column width
             */
            columnW: null,

            /**
             * Number of weeks in current trimester
             */
            weeks: null,

            /**
             * Graph scale settings
             */
            minValue: null,
            maxValue: null,
            scale: null,
            id: null, //canvas parent id

            layer: null,
            layer_id: null,
            ctx: null,

            /**
             * Load JSON file
             *
             * @return void
             * @author Mariusz Buk
             */
            _load: function (reload)
            {
                var that = this;

                $.ajax({
                    url: this.widget.options.url,
                    dataType: 'json',
                    //contentType: 'application/json',
                    crossDomain: true,
                    type: 'get',
                    error: function (jqXHR, textStatus, errorThrown)
                    {
                        if (typeof console !== "undefined")
                        {
                            // console.log(jqXHR, textStatus, errorThrown);
                        }
                    },
                    success: function (data)
                    {
                        that.weeks = trimester_array [that.widget.options.trimester-1][1] - trimester_array [that.widget.options.trimester-1][0] + 1;

                        that.json = data;

                        that.canvas.width = that.json.width;

                        // set default column width
                        that.columnW = (that.json.width - margin.left - margin.right - margin.innerRight - margin.innerLeft) / that.weeks;

                        // draw
                        that._drawBackground();
                        that._drawTrimesters();
                        that._calculateScale();
                        that._drawGrid();
                        that._drawPercentilLines();
                        that._drawLines();
                        that._drawPoints();
                        that._highlightCurrentDay();

                        // do not create layer or attach events if graph should be reloaded
                        if (typeof reload === "undefined")
                        {
                            that._createLayer();
                            that._createEvents();
                        }

                        that._drawLegend();
                    }
                });
            },

            /**
             * Draw background
             *
             * @return void
             * @author Mariusz Buk
             */
            _drawBackground: function()
            {
                this.context.lineWidth = 1;
                this.context.lineHeight = 1;
                this.context.strokeStyle = '#d3b5df';

                // clear background
                this.context.clearRect(0, 0, this.json.width, GRAPH_HEIGHT);

                // frame
                this.context.strokeRect(0.5, 0.5, this.json.width - margin.right + 9, GRAPH_HEIGHT-1);

                return;

                // right background
                this.context.fillStyle = '#ebd3f5';
                this.context.fillRect(this.json.width - margin.right + 10, 0, margin.right - 10, GRAPH_HEIGHT);
            },

            /**
             * Draw legend
             *
             * @returns void
             * @author Mariusz Buk
             */
            _drawLegend: function()
            {
                var x = margin.left + margin.innerLeft, y = margin.top + margin.innerTop;

                // legend
                this.context.lineWidth = 1;
                this.context.lineHeight = 1;
                this.context.strokeStyle = "rgba(0,0,0,0.5)";
                this.context.fillStyle = "white";

                /* filled rounded rectangle
                this.roundRect(this.context, margin.left + margin.innerLeft + 0.5, margin.top + 10 + 0.5,
                    250, 30, 10, true, true);
                this.roundRect(this.context, margin.left + margin.innerLeft + 0.5, margin.top + 10 + 40 + 0.5,
                    250, 30, 10, true, true);
                */

                this.context.fillStyle = "#21aced";
                this.roundRect(this.context, x + 5 + 0.5, y + 5 + 0.5,
                    20, 20, 5, true, false);
                this.context.fillStyle = "#d3b5df";
                this.roundRect(this.context, x + 5 + 0.5, y + 35 + 0.5,
                    20, 20, 5, true, false);

                this.context.font = "14px Arial";
                this.context.textAlign = "left";
                this.context.textBaseline = "middle";
                this.context.fillStyle = "black";
                this.context.fillText(t("Twoja waga"), x + 30 + 0.5, y + 5 + 10 + 0.5);
                this.context.fillText(t("Zalecany przyrost wagi dla Ciebie"), x + 30 + 0.5, y + 45 + 0.5);
            },

            /**
             * Draw trimester selections
             *
             * @return void
             * @author Mariusz Buk
             */
            _drawTrimesters: function()
            {
                var i, x, y, w, h, r, b, radius;

                return;

                x = this.json.width - margin.right + 10 - 0.5;
                y = 0.5;
                w = 30;
                h = 50;
                radius = 10;

                this.context.fillStyle = 'white';
                this.context.lineWidth = 1;
                this.context.lineHeight = 1;

                this.context.font = "18px Georgia";
                this.context.textAlign = "center";
                this.context.textBaseline = "middle";

                for (i = 0; i < 3; i++)
                {
                    r = x + w;
                    b = y + h;

                    this.context.strokeStyle = "#d3b5df";
                    if  (this.widget.options.trimester === i + 1)
                    {
                        this.context.fillStyle = 'white';
                    }
                    else
                    {
                        this.context.fillStyle = '#f9effd';
                    }
                    this.context.beginPath();
                    this.context.moveTo(x, y);
                    this.context.lineTo(r - radius, y);
                    this.context.quadraticCurveTo(r, y, r, y + radius);
                    this.context.lineTo(r, b - radius);
                    this.context.quadraticCurveTo(r, b, r - radius, b);
                    this.context.lineTo(x, b);
                    this.context.fill();
                    this.context.stroke();
                    if  (this.widget.options.trimester === i + 1)
                    {
                        this.context.strokeStyle = "white";
                        this.context.beginPath();
                        this.context.moveTo(x, y + 1);
                        this.context.lineTo(x, b - 1);
                        this.context.stroke();

                        this.context.fillStyle = 'black';
                    }
                    else
                    {
                        this.context.fillStyle = 'rgba(0,0,0,0.5)';
                    }

                    // trimester number
                    this.context.fillText(i === 0 ? "I" : (i === 1 ? "II" : "III"), x + w / 2, y + h / 2);

                    y += h + 10;
                }
            },

            /**
             * Check if mouse is over the trimester choice
             *
             * @param mousePos x, y
             * @return int|false
             */
            _inTrimester: function(mousePos)
            {
                var i, x, y, w, h, r, b, radius;

                return false;

                x = this.json.width - margin.right + 10 - 0.5;
                y = 0.5;
                w = 30;
                h = 50;
                r = x + w;

                if (mousePos.x < x || mousePos.x > r) return false;

                for (i = 0; i < 3; i++)
                {
                    b = y + h;

                    if (mousePos.y >= y && mousePos.y <= b)
                    {
                        return i + 1;
                    }

                    y += h + 10;
                }

                return false;
            },

            /**
             * Calculate graph scale
             *
             * @author Mariusz Buk
             */
            _calculateScale: function()
            {
                var i, postfix, column, start, end, int;

                this.minValue = 1000;
                this.maxValue = 0;
                postfix = this.widget.options.unit === "metric" ? "kg" : "lbs";

                // find min and max values in centile table for current trimester only
                start = trimester_array [0][0] - 1;
                end = trimester_array [2][1];
                for (i = start; i < end; i++)
                {
                    column = this.json.weeks[i];

                    int = parseInt(column["min_weight_"+postfix]);
                    if (int > 0 && int < this.minValue)
                    {
                        this.minValue = int;
                    }
                    int = parseInt(column["max_weight_"+postfix]);
                    if (int > 0 && int > this.maxValue)
                    {
                        this.maxValue = int;
                    }
                }

                // find min and max values in user weight in current trimester only
                start = (trimester_array [0][0] - 1) * 7;
                end = (trimester_array [2][1]) * 7;
                for (i = start; i < end; i++)
                {
                    column = this.json.days[i];

                    int = parseInt(column["weight_"+postfix]);
                    if (int > 0 && int < this.minValue)
                    {
                        this.minValue = int;
                    }
                    if (int > 0 && int > this.maxValue)
                    {
                        this.maxValue = int;
                    }
                }

                // add margin
                this.minValue = Math.floor(this.minValue);
                this.maxValue = Math.ceil(this.maxValue) + 1;

                // calculate difference
                this.scale = this.maxValue - this.minValue;
            },

            /**
             * Draw grid - horizontal and vertical lines
             *
             * @return void
             * @author Mariusz Buk
             */
            _drawGrid: function ()
            {
                var i, work_height, work_width, graduation, graduation_px, step, left, top, size, x, y;

                this.context.lineWidth = 1;
                this.context.lineHeight = 1;
                this.context.strokeStyle = 'rgba(0,0,0,1)';

                // vertical and horizontal solid line
                this.context.beginPath();
                this.context.moveTo(margin.left, margin.top);
                this.context.lineTo(margin.left, GRAPH_HEIGHT - margin.bottom);
                this.context.lineTo(this.json.width - margin.right, GRAPH_HEIGHT - margin.bottom);
                this.context.stroke();

                /**
                 * podziałka przy skali pionowej
                 */

                // wysokość robocza
                work_height = GRAPH_HEIGHT - margin.bottom - margin.innerBottom - margin.innerTop - margin.top;
                // szerokość robocza
                work_width = this.json.width - margin.right;
                // oblicz podziałkę co ile kg
                step = 0.5; //this.scale / (10 - 1);
                // ile podziałek
                graduation = Math.floor(this.scale / step) + 1;

                // jeżeli podziałek jest więcej niż 10 skoryguj to aby wykres nie był nieczytelny
                while (graduation > 10)
                {
                    step *= 2;
                    // korekta liczby podziałek
                    graduation = Math.ceil(this.scale / step) + 1;
                }

                // podziałka co ile px
                graduation_px = work_height / (graduation - 1);
                // wielkość podziałki w px
                size = 10;
                // początek x pionowej podziałki
                left = margin.left - (size / 2);

                // zabezpieczenie przed złymi danymi
                if (graduation <= 0)
                {
                    tools.handleError("Nieprawidłowe dane dla wykresu "+this.widget.options.url, true);
                    tools.alert(t("Przepraszamy, ale w tej chwili nie możemy wygenerować wykresu. Administrator został powiadomiony."));
                    return;
                }

                // siatka pozioma
                this.context.strokeStyle = 'rgba(0,0,0,0.1)';
                for (i = 0; i < graduation; i++)
                {
                    y = Math.round(i * graduation_px + margin.innerTop + margin.top) + 0.5;
                    this._drawDashedLine(left + size + 5, y, work_width, y, 4);
                }

                // podziałka na osi Y
                this.context.lineWidth = 1;
                this.context.lineHeight = 1;
                this.context.strokeStyle = 'rgba(0,0,0,1)';

                this.context.beginPath();
                for (i = 0; i < graduation; i++)
                {
                    y = Math.round(i * graduation_px + margin.innerTop + margin.top) + 0.5;
                    // duża
                    this.context.moveTo(left, y);
                    this.context.lineTo(left + size, y);
                    // mała
                    if (i < graduation - 1)
                    {
                        this.context.moveTo(left + 3, y + Math.round(graduation_px / 2));
                        this.context.lineTo(left + size - 3, y + Math.round(graduation_px / 2))
                    }
                }
                this.context.stroke();

                // wypisz wagę na osi Y
                this.context.font = "11px Arial";
                this.context.fillStyle = 'rgba(0,0,0,1)';
                this.context.textAlign = "right";
                this.context.textBaseline = "middle";
                for (i = 0; i < graduation; i++)
                {
                    this.context.fillText(sprintf("%0.1f", this.maxValue-i*step), left -5, margin.innerTop + margin.top + i * graduation_px);
                }

                /**
                 * podziałka przy skali poziomej
                 */

                // podziałka co 1 tydzień
                step = 1;
                // szerokość robocza
                work_width = this.json.width - margin.left - margin.innerRight - margin.innerLeft - margin.right;
                // ile podziałek
                graduation = this.weeks;
                // podziałka co
                graduation_px = work_width / graduation;
                // wielkość podziałki w px
                size = 10;
                // początek x pionowej podziałki
                top = GRAPH_HEIGHT - margin.bottom - (size / 2);

                this.context.beginPath();
                for (i = 0; i <= graduation; i++)
                {
                    x = Math.round(margin.left + margin.innerRight + i*graduation_px) + 0.5;
                    this.context.moveTo(x, top);
                    this.context.lineTo(x, top + size);
                }
                this.context.stroke();

                /**
                 * Week number
                 */
                this.context.font = "11px Arial";
                this.context.fillStyle = 'rgba(0,0,0,1)';
                this.context.textAlign = "center";
                this.context.textBaseline = "top";
                for (i = 0; i < graduation; i++)
                {
                    x =  margin.left + margin.innerLeft + i*graduation_px + this.columnW / 2;
                    this.context.fillText(i + trimester_array [this.widget.options.trimester-1][0], x, top + size + 5);
                }

                // tytuł wykresu wagi
                this.context.font = "bold 16px Arial";
                this.context.fillStyle = 'rgba(0,0,0,1)';
                this.context.textAlign = "center";
                this.context.textBaseline = "top";
                this.context.fillText(
                    "Twój wykres wagi - trymestr " +
                    (this.widget.options.trimester === 1 ? "I" : (this.widget.options.trimester === 2 ? "II" : "III")),
                    this.json.width / 2, 10
                );

                // opis osi X
                this.context.font = "bold 16px Arial";
                this.context.fillStyle = 'rgba(0,0,0,1)';
                this.context.textAlign = "center";
                this.context.textBaseline = "middle";
                this.context.fillText(t("TYDZIEŃ CIĄŻY"), this.json.width / 2, GRAPH_HEIGHT - 15);

                // opis osi Y
                this.context.font = "bold 16px Arial";
                this.context.fillStyle = 'rgba(0,0,0,1)';
                this.context.textAlign = "center";
                this.context.textBaseline = "middle";
                this.context.save();
                this.context.translate(15, (GRAPH_HEIGHT - margin.bottom)/2);
                this.context.rotate(-Math.PI/2);
                if (this.widget.options.unit === 'metric')
                {
                    this.context.fillText(t("WAGA (kg)"), 0, 0);
                }
                else
                {
                    this.context.fillText(t("WAGA (lbs)"), 0, 0);
                }
                this.context.restore();
            },

            /**
             * Narysuj linię wagi
             */
            _drawLines: function ()
            {
                var i, index, color, postfix, column, next_column, x, start, end;

                postfix = this.widget.options.unit === "metric" ? "kg" : "lbs";
                color = '#21aced';

                start = (trimester_array [this.widget.options.trimester-1][0] - 1) * 7;
                end = (trimester_array [this.widget.options.trimester-1][1]) * 7;
                for (i = start; i < end; i++)
                {
                    // search next not empty value
                    index = this._searchNextValue(i);

                    column = this.json.days[i];
                    if (column.dot === "full" || column.dot === "empty")
                    {
                        if (index > 0)
                        {
                            next_column = this.json.days[index];

                            this.context.strokeStyle = color;
                            this.context.lineWidth = 2;
                            this.context.lineHeight = 2;

                            x = margin.left + margin.innerLeft + (i - start + 0.5) * (this.columnW / 7);
                            this.context.beginPath();
                            this.context.moveTo(x, this._getY(column["weight_"+postfix]));
                            x = margin.left + margin.innerLeft + (index - start + 0.5) * (this.columnW / 7);
                            this.context.lineTo(x, this._getY(next_column["weight_"+postfix]));
                            this.context.stroke();
                        }
                    }
                }
            },

            /**
             * Narysuj linię minimum i maksimum
             *
             * @return void
             * @author Mariusz Buk
             */
            _drawPercentilLines: function ()
            {
                var i, postfix, polygon1 = [], polygon2 = [], polygons, column, x, start, end, diff;

                postfix = this.widget.options.unit === "metric" ? "kg" : "lbs";

                start = trimester_array [this.widget.options.trimester-1][0] - 1;
                end = trimester_array [this.widget.options.trimester-1][1];
                for (i = start; i < end; i++)
                {
                    column = this.json.weeks[i];
                    if (column["min_weight_"+postfix])
                    {
                        x = margin.left + margin.innerLeft + (i - start + 0.5) * this.columnW;

                        if (i === start)
                        {
                            diff = (this.json.weeks[i + 1]["min_weight_"+postfix] - column["min_weight_"+postfix]) / 2;
                            if (column["max_weight_"+postfix] === column["min_weight_"+postfix]) diff = 0;
                            polygon1.push([x - (this.columnW / 2), this._getY(parseFloat(column["min_weight_"+postfix]) - diff)]);
                            diff = (this.json.weeks[i + 1]["max_weight_"+postfix] - column["max_weight_"+postfix]) / 2;
                            if (column["max_weight_"+postfix] === column["min_weight_"+postfix]) diff = 0;
                            polygon2.push([x - (this.columnW / 2), this._getY(parseFloat(column["max_weight_"+postfix]) - diff)]);
                        }

                        polygon1.push([x, this._getY(column["min_weight_"+postfix])]);
                        polygon2.push([x, this._getY(column["max_weight_"+postfix])]);

                        if (i === end - 1)
                        {
                            diff = (column["min_weight_"+postfix] - this.json.weeks[i - 1]["min_weight_"+postfix]) / 2;
                            polygon1.push([x + (this.columnW / 2), this._getY(parseFloat(column["min_weight_"+postfix]) + diff)]);
                            diff = (column["max_weight_"+postfix] - this.json.weeks[i - 1]["max_weight_"+postfix]) / 2;
                            polygon2.push([x + (this.columnW / 2), this._getY(parseFloat(column["max_weight_"+postfix]) + diff)]);
                        }
                    }
                }

                polygons = polygon1.concat(polygon2.reverse());

                this.context.strokeStyle = '#D3B5DF';
                this.context.fillStyle = "rgba(235, 211, 245, 0.5)";
                this.context.lineWidth = 2;
                this.context.lineHeight = 2;

                this.context.beginPath();
                for (i = 0; i < polygons.length; i++)
                {
                    if (i === 0)
                    {
                        this.context.moveTo(polygons[i][0], polygons[i][1]);
                    }
                    else
                    {
                        this.context.lineTo(polygons[i][0], polygons[i][1]);
                    }
                }
                this.context.closePath();
                this.context.stroke();
                this.context.fill();
            },

            /**
             * Set current and disabled days. Do not set current day if graph is not editable.
             *
             * @return void
             * @author Mariusz Buk
             */
            _highlightCurrentDay: function ()
            {
                var offset;

                this.context.lineWidth = 1;

                offset =  this.widget.options.week- trimester_array [this.widget.options.trimester-1][0];
                this.context.fillStyle = "rgba(0,0,0,0.1)";
                this.context.fillRect(
                    margin.left + margin.innerLeft + offset * this.columnW,
                    margin.top,
                    this.columnW,
                    GRAPH_HEIGHT - margin.bottom - margin.top
                );
            },

            /*
             * Zwraca numer kolumny, na której znajduje sie kursor (od 1)
             *
             * @return int
             * @author Mariusz Buk
             */
            _checkColumn: function (pos)
            {
                var c = false;

                if (pos.x > margin.left + margin.innerLeft &&
                    pos.x < this.json.width - margin.innerRight - margin.right &&
                    pos.y < GRAPH_HEIGHT - margin.bottom)
                {
                    c = Math.ceil((pos.x - margin.left - margin.innerLeft) / this.columnW);
                }

                return c;
            },

            /**
             * oblicza względną pozycję kursora
             **/
            getMousePos: function (c, e)
            {
                var rect, mouseX, mouseY;

                rect = c.getBoundingClientRect();
                mouseX = e.clientX - rect.left;
                mouseY = e.clientY - rect.top;

                return {
                    x: parseInt(mouseX, 10),
                    y: parseInt(mouseY, 10)
                };
            },

            /**
             * "Animuje" punkty wykresu.
             *
             * @param c Numer kolumny od 1
             * @param mousePos Obiekt pozycji myszki
             * @author Mariusz Buk
             */
            _enlargePoint: function (c, mousePos)
            {
                var color, g, x, y, column, text, text_width, text_height, postfix, day_c,
                    days_string, week_string;

                if  (this.widget.options.editable)
                {
                    $(this.layer).css('cursor', 'pointer');
                }
                // eval('g=' + this.json.columns[c].onhover);
                // g();

                postfix = this.widget.options.unit === "metric" ? "kg" : "lbs";
                this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

                // vertical solid line
                /*
                this.ctx.lineWidth = 1;
                this.ctx.lineHeight = 1;
                this.ctx.strokeStyle = '#f1d031';

                this.ctx.beginPath();
                x = Math.round(margin.left + margin.innerLeft + c * this.columnW - this.columnW/2) + 0.5;
                this.ctx.moveTo(x, margin.top);
                this.ctx.lineTo(x, GRAPH_HEIGHT - margin.bottom);
                this.ctx.stroke();
                */

                // highlight column
                x = Math.round(margin.left + margin.innerLeft + (c-1) * this.columnW) + 0.5;
                this.ctx.fillStyle = "rgba(241, 208, 49, 0.2)";
                this.ctx.fillRect(
                    x,
                    margin.top,
                    this.columnW,
                    GRAPH_HEIGHT - margin.bottom - margin.top
                );

                day_c = Math.ceil((mousePos.x - margin.left - margin.innerLeft) / (this.columnW / 7));
                column = this.json.days[(trimester_array [this.widget.options.trimester - 1][0] - 1) * 7 + day_c - 1];
                color = '#21aced';

                // print weight in bubble speach
                text = t("Kliknij aby wpisać wagę");
                text_width = this.ctx.measureText(text).width;
                this.drawBubble(
                    mousePos.x + 10,
                    mousePos.y + 10 /*this._getY(column["weight_"+postfix]) + 10 */,
                    text_width + 20,
                    column['weight_'+postfix] > 0 ? 100 : 80,
                    10
                );

                this.ctx.font = "12px Arial";
                this.ctx.textAlign = "left";
                this.ctx.textBaseline = "top";
                this.ctx.fillStyle = 'black';
                y = mousePos.y + 20;
                // data
                this.ctx.fillText(app.common.getDate(column.date, "long_numeric"), mousePos.x + 20, y);
                if (column['weight_'+postfix] > 0)
                {
                    y += 20;
                    // waga
                    this.ctx.fillText(column['weight_'+postfix] + " " + postfix, mousePos.x + 20, y);
                }
                y += 20;
                // długość ciąży
                if (column.day != 1)
                {
                    days_string = t("dni");
                }
                else
                {
                    days_string = t("dzień");
                }
                if (column.week == 2)
                {
                    week_string = t("tydz.");
                }
                else
                {
                    week_string = t("tyg.");
                }
                this.ctx.fillText(
                    (column.week > 1 ? (column.week - 1) + " " + week_string + " + " : "") + column["day"] + " " + days_string,
                    mousePos.x + 20, y
                );
                y += 20;
                // przedział tygodnia
                this.ctx.fillText(text, mousePos.x + 20, y);

                // bigger dots
                x = Math.round(margin.left + margin.innerLeft + (day_c - 0.5) * (this.columnW / 7));
                if (column.dot === "full")
                {
                    this.ctx.beginPath();
                    this.ctx.fillStyle = color;
                    this.ctx.arc(
                        x,
                        this._getY(column["weight_"+postfix]),
                        6, 0, Math.PI * 2, false
                    );
                    this.ctx.fill();
                }
                else if (column.dot === "empty")
                {
                    this.ctx.fillStyle = "#ffffff";
                    this.ctx.lineWidth = 2;
                    this.ctx.lineHeight = 2;
                    this.ctx.strokeStyle = color;
                    this.ctx.beginPath();
                    this.ctx.arc(
                        x,
                        this._getY(column["weight_"+postfix]),
                        6, 0, Math.PI * 2, false
                    );
                    this.ctx.stroke();
                    this.ctx.fill();
                }

                // text
                /*
                this.ctx.font = "bold 12px Arial";
                this.ctx.textAlign = "center";
                this.ctx.textBaseline = "top";
                text = column.date_from+" - "+column.date_to;
                text_width = this.ctx.measureText(text).width;
                text_height = 12;
                if (x + text_width/2 + 5 > this.json.width)
                {
                    x = this.json.width - text_width/2 - 5;
                }
                this.ctx.strokeStyle = '#f1d031';
                this.ctx.lineJoin = "round";
                this.ctx.lineWidth = 1;
                this.ctx.strokeRect(x - text_width / 2 - 5, GRAPH_HEIGHT - margin.bottom + 15 - text_height/2,
                    text_width + 10, text_height + 15);
                this.ctx.fillStyle = '#ffffa3';
                this.ctx.lineJoin = "round";
                this.ctx.fillRect(x - text_width / 2 - 5, GRAPH_HEIGHT - margin.bottom + 15 - text_height/2,
                    text_width + 10, text_height + 15);
                this.ctx.fillStyle = 'white';
                this.ctx.fillText(text, x, GRAPH_HEIGHT - margin.bottom + 15 + 1);
                this.ctx.fillStyle = 'black';
                this.ctx.fillText(text, x, GRAPH_HEIGHT - margin.bottom + 15);
                */
            },

            drawBubble: function (x, y, w, h, radius)
            {
                var r = x + w;
                var b = y + h;
                this.ctx.beginPath();
                this.ctx.strokeStyle = "#f1d031";
                this.ctx.fillStyle = '#ffffa3';
                this.ctx.lineWidth = "2";
                this.ctx.moveTo(x + radius, y);
                this.ctx.lineTo(x + radius / 2, y - 10);
                this.ctx.lineTo(x + radius * 2, y);
                this.ctx.lineTo(r - radius, y);
                this.ctx.quadraticCurveTo(r, y, r, y + radius);
                this.ctx.lineTo(r, y + h - radius);
                this.ctx.quadraticCurveTo(r, b, r - radius, b);
                this.ctx.lineTo(x + radius, b);
                this.ctx.quadraticCurveTo(x, b, x, b - radius);
                this.ctx.lineTo(x, y + radius);
                this.ctx.quadraticCurveTo(x, y, x + radius, y);
                this.ctx.stroke();
                this.ctx.fill();
            },

            /**
             * Tworzy warstwę "animacji"
             *
             * @return void
             * @author Adam Lebioda
             */
            _createLayer: function ()
            {
                var c, zindex;

                c = document.createElement('canvas');
                if (typeof G_vmlCanvasManager !== 'undefined') {
                    c = G_vmlCanvasManager.initElement(c); //for IE < 9
                }
                c.id = this.id + '_canvas_layer';
                this.layer_id = c.id;
                $('#' + this.id).append(c);
                zindex = this.widget.element.css("zIndex");
                zindex = typeof zindex === "undefined" || !zindex ? 1 : zindex + 1;
                $('#' + c.id).css({
                    position: 'absolute',
                    zIndex: zindex,
                    top: 0, left: 0
                });
                this.layer = document.getElementById(c.id);
                this.layer.width = this.json.width;
                this.layer.height = GRAPH_HEIGHT;
                this.ctx = this.layer.getContext('2d');
            },

            /**
             * Create Events
             */
            _createEvents: function ()
            {
                var call, event_prefix, mouse_over = false, that = this;

                if (typeof this.layer.addEventListener !== "undefined")
                {
                    call = this.layer.addEventListener;
                    event_prefix = "";
                }
                else if (typeof this.layer.attachEvent !== "undefined")
                {
                    call = this.layer.attachEvent;
                    event_prefix = "on";
                }
                else
                    return;

                $(this.layer).mouseenter(function (e)
                {
                    mouse_over = true;
                });

                call(event_prefix+'mousemove', function (e)
                {
                    var mousePos, c, trimester;

                    if (mouse_over)
                    {
                        mousePos = that.getMousePos(that.layer, e);
                        if (mousePos.x > 0 && mousePos.x < that.json.width)
                        {
                            c = that._checkColumn(mousePos);
                            if (c)
                            {
                                that._enlargePoint(c, mousePos);
                            }
                            else
                            {
                                that.ctx.clearRect(0, 0, that.layer.width, that.layer.height);

                                trimester = that._inTrimester(mousePos);

                                if (trimester)
                                {
                                    $(that.layer).css('cursor', 'pointer');
                                }
                                else
                                {
                                    $(that.layer).css('cursor', 'auto');
                                }
                            }
                        }
                    }
                });

                $(this.layer).mouseleave(function ()
                {
                    mouse_over = false;
                    that.ctx.clearRect(0, 0, that.layer.width, that.layer.height);
                    $(that.layer).css('cursor', 'auto');
                    app.graph.hint = '';
                });

                $(this.layer).on('click', function (e)
                {
                    var mousePos, c, column, day_c, trimester;

                    mousePos = that.getMousePos(that.layer, e);
                    c = that._checkColumn(mousePos);
                    if (c &&  that.widget.options.editable)
                    {
                        day_c = Math.ceil((mousePos.x - margin.left - margin.innerLeft) / (that.columnW / 7)) +
                            (trimester_array[that.widget.options.trimester - 1][0] - 1) * 7;
                        column = that.json.days[day_c - 1];

                        // external call
                        app.bellyfriend_mother.motherClick(day_c, column);
                    }
                    else
                    {
                        trimester = that._inTrimester(mousePos);

                        // zmiana trymestru
                        if (trimester && trimester !=  that.widget.options.trimester)
                        {
                            app.pregnancy.changeTrimester(trimester);

                            // change trimester
                            that.widget.options.trimester = trimester;
                            // calculate number of weeks
                            that.weeks = trimester_array [that.widget.options.trimester-1][1] - trimester_array [that.widget.options.trimester-1][0] + 1;
                            // set default column width
                            that.columnW = (that.canvas.width - margin.left - margin.right - margin.innerRight - margin.innerLeft) / that.weeks;

                            // redraw
                            that._drawBackground();
                            that._drawTrimesters();
                            that._calculateScale();
                            that._drawGrid();
                            that._drawPercentilLines();
                            that._drawLines();
                            that._drawPoints();
                            that._highlightCurrentDay();
                            that._drawLegend();
                        }
                    }
                });
            },

            /**
             * Draw dashed line
             */
            _drawDashedLine: function (x1, y1, x2, y2, dashLength) {

                var deltaX, deltaY, numDashes, i;

                dashLength = dashLength === undefined ? 5 : dashLength;
                deltaX = x2 - x1;
                deltaY = y2 - y1;
                numDashes = Math.floor(Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) / dashLength);

                for (i = 0; i <= numDashes; i++) {
                    if (typeof G_vmlCanvasManager !== 'undefined') {
                        this.context[i % 2 === 0 ? 'moveTo' : 'lineTo'](x1 + (deltaX / numDashes) * i, y1 + (deltaY / numDashes) * i);
                        //(parseInt(x1 + (deltaX / numDashes) * i), parseInt(y1 + (deltaY / numDashes) * i));
                    } else {
                        this.context[i % 2 === 0 ? 'moveTo' : 'lineTo'](x1 + (deltaX / numDashes) * i, y1 + (deltaY / numDashes) * i);
                    }
                }

                this.context.stroke();
            },

            /**
             * Draw temperature/weight points
             *
             * @return void
             * @author Mariusz Buk
             */
            _drawPoints: function ()
            {
                var i, color, column, postfix, x, start, end;

                postfix = this.widget.options.unit === "metric" ? "kg" : "lbs";
                color = '#21aced';
                start = (trimester_array [this.widget.options.trimester-1][0] - 1) * 7;
                end = trimester_array [this.widget.options.trimester-1][1] * 7;
                for (i = start; i < end; i++)
                {
                    column = this.json.days[i];

                    if (column.dot === "full")
                    {
                        this.context.beginPath();
                        this.context.fillStyle = color;
                        x = margin.left + margin.innerLeft + (i - start + 0.5) * (this.columnW / 7);
                        this.context.arc(
                            x,
                            this._getY(column["weight_"+postfix]),
                            4, 0, Math.PI * 2, false
                        );
                        this.context.fill();
                    }
                    else if (column.dot === "empty")
                    {
                        this.context.fillStyle = "#ffffff";
                        this.context.lineWidth = 2;
                        this.context.lineHeight = 2;
                        this.context.strokeStyle = color;
                        this.context.beginPath();
                        x = margin.left + margin.innerLeft + (i - start + 0.5) * (this.columnW / 7);
                        this.context.arc(
                            x,
                            this._getY(column["weight_"+postfix]),
                            3, 0, Math.PI * 2, false
                        );
                        this.context.stroke();
                        this.context.fill();
                    }

                    // dla pozostałych przypadków this.json.columns[i].dot == "none" nic nie rysujemy
                }
            },

            /**
             * Search next weight
             *
             * @return int next day index (or 0 if no future values)
             */
            _searchNextValue: function (index)
            {
                var i, result = 0, end;

                end = (trimester_array [this.widget.options.trimester-1][1]) * 7;
                for (i = index + 1; i < end; i++)
                {
                    if (this.json.days[i].weight_kg > 0)
                    {
                        result = i;
                        break;
                    }
                }

                return result;
            },

            /**
             * Get vertical postition based on weight
             */
            _getY: function (value)
            {
                var work_height, y;

                // max y point - work_height * (weight - min_weight) / scale

                work_height = GRAPH_HEIGHT - margin.bottom - margin.innerBottom - margin.top - margin.innerTop;

                y = Math.round((GRAPH_HEIGHT - margin.bottom - margin.innerBottom) - work_height * (value - this.minValue) / this.scale);

                return y;
            },

            /**
             * Convert color from hex (eg. 0x112233) to www (eg. #112233)
             */
            _convertColor: function (color)
            {
                return '#' + color.match(/0x([\da-f]+)/i)[1];
            },

            /**
             * Draws a rounded rectangle using the current state of the canvas.
             * If you omit the  last three params, it will draw a rectangle
             * outline with a 5 pixel border radius
             *
             * @param {CanvasRenderingContext2D} ctx
             * @param {Number} x The top left x coordinate
             * @param {Number} y The top left y coordinate
             * @param {Number} width The width of the rectangle
             * @param {Number} height The height of the rectangle
             * @param {Number} radius The corner radius. Defaults to 5;
             * @param {Boolean} fill Whether to fill the rectangle. Defaults to false.
             * @param {Boolean} stroke Whether to stroke the rectangle. Defaults to true.
            */
            roundRect: function (ctx, x, y, width, height, radius, fill, stroke)
            {
                if (typeof stroke === "undefined")
                {
                    stroke = true;
                }
                if (typeof radius === "undefined")
                {
                    radius = 5;
                }
                ctx.beginPath();
                ctx.moveTo(x + radius, y);
                ctx.lineTo(x + width - radius, y);
                ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
                ctx.lineTo(x + width, y + height - radius);
                ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
                ctx.lineTo(x + radius, y + height);
                ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
                ctx.lineTo(x, y + radius);
                ctx.quadraticCurveTo(x, y, x + radius, y);
                ctx.closePath();
                if (stroke)
                {
                    ctx.stroke();
                }
                if (fill)
                {
                    ctx.fill();
                }
            }
        },

        /**
         * Create graph
         *
         * @returns void
         */
        _create: function()
        {
            // create canvas
            var canvas;

            if (this.prv.id !== null)
            {
                // change trimester
//                this._change();
            }
            else
            {
                // get json url
                if (!this.options.url && typeof this.element.data('src') !== "undefined")
                {
                    this.options.url = this.element.data('src');
                }

                // get unit type
                if (!this.options.unit && typeof this.element.data('unit') !== "undefined")
                {
                    this.options.unit = this.element.data('unit');
                }

                // set trimester
                if (!this.options.trimester && typeof this.element.data('trimester') !== "undefined")
                {
                    this.options.trimester = this.element.data('trimester');
                }
                else
                {
                    this.options.trimester = 1;
                }

                // set week
                if (!this.options.week && typeof this.element.data('week') !== "undefined")
                {
                    this.options.week = this.element.data('week');
                }
                else
                {
                    this.options.week = 1;
                }

                canvas = document.createElement('canvas');
                if (typeof G_vmlCanvasManager !== 'undefined') {
                    canvas = G_vmlCanvasManager.initElement(canvas); //for IE < 9
                }

                // init canvas
                this.prv.id = this.element.attr("id");
                canvas.id = this.prv.id + "_canvas";
                this.element.append(canvas);
                $('#' + this.prv.id + "_canvas").css('background-color', '#ffffff');
                this.prv.canvas = document.getElementById(this.prv.id + "_canvas");
                this.prv.canvas.height = GRAPH_HEIGHT;
                this.prv.context = this.prv.canvas.getContext('2d');
                this.prv.widget = this;

                // load and draw
                this.prv._load();
            }
        },

        /**
         * Set options
         *
         * @param key
         * @param value
         * @returns void
         */
        _setOption: function(key, value)
        {
            this.options[key] = value;
            this._change();
        },

        /**
         * Redraw graph
         *
         * @return void
         * @author Mariusz Buk
         */
        _change: function ()
        {
            // calculate number of weeks
            this.prv.weeks = trimester_array [this.options.trimester - 1][1] - trimester_array [this.options.trimester - 1][0] + 1;

            // set default column width
            this.prv.columnW = (this.prv.json.width - margin.left - margin.right - margin.innerRight - margin.innerLeft) / this.prv.weeks;

            // redraw
            this.prv._drawBackground();
            this.prv._drawTrimesters();
            this.prv._calculateScale();
            this.prv._drawGrid();
            this.prv._drawPercentilLines();
            this.prv._drawLines();
            this.prv._drawPoints();
            this.prv._highlightCurrentDay();
            this.prv._drawLegend();
        },

        /**
         * Redraw graph
         *
         * @return void
         * @author Mariusz Buk
         */
        update: function (url)
        {
            this.options.url = url;

            this.prv._load(true);
        },

        /**
         * Remove all elements
         *
         * @returns void
         */
        destroy: function ()
        {
            this.element.find("canvas").remove();

            // Call the base destroy function.
            $.Widget.prototype.destroy.call(this);
        }
    });
}) (jQuery);