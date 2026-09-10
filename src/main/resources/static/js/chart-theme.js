const ChartTheme = {
    fontFamily: 'system-ui, -apple-system, "Segoe UI", sans-serif',
    textSecondary: '#52514e',
    textMuted: '#898781',
    gridline: '#e1e0d9',
    baseline: '#c3c2b7',
    series: [
        { border: '#2a78d6', wash: 'rgba(42, 120, 214, 0.10)' },
        { border: '#eb6834', wash: 'rgba(235, 104, 52, 0.10)' },
    ],
};

const SPARSE_DATA_POINT_THRESHOLD = 10;

function applyChartDefaults() {
    Chart.defaults.font.family = ChartTheme.fontFamily;
    Chart.defaults.color = ChartTheme.textSecondary;
    Chart.defaults.borderColor = ChartTheme.gridline;
}

function countDataPoints(data) {
    return data.filter(function (value) {
        return value !== null && value !== undefined;
    }).length;
}

function isSparseData(data) {
    return countDataPoints(data) <= SPARSE_DATA_POINT_THRESHOLD;
}

function resolvePointRadius(data) {
    // A dense series hides per-point markers for a clean line (hover reveals them);
    // a sparse series (as few as one point) needs a visible dot, or nothing renders at all.
    return isSparseData(data) ? 4 : 0;
}

function resolvePointHoverRadius(data) {
    return isSparseData(data) ? 6 : 4;
}

function buildLineDataset(label, data, seriesIndex, options) {
    const series = ChartTheme.series[seriesIndex];
    return Object.assign(
        {
            label: label,
            data: data,
            borderColor: series.border,
            backgroundColor: series.wash,
            borderWidth: 2,
            pointRadius: resolvePointRadius(data),
            pointHoverRadius: resolvePointHoverRadius(data),
            tension: 0.2,
            fill: true,
        },
        options,
    );
}

function buildAxisOptions(titleText) {
    return {
        display: true,
        title: { display: true, text: titleText, color: ChartTheme.textSecondary },
        grid: { color: ChartTheme.gridline },
        ticks: { color: ChartTheme.textMuted },
    };
}

applyChartDefaults();
