import React, { useState, useRef, useMemo, useCallback } from 'react';
import Map, { Source, Layer, MapRef, FillLayer, LineLayer } from 'react-map-gl/maplibre';
import 'maplibre-gl/dist/maplibre-gl.css';
import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';

// MapLibre Styling for Parcels
const fillLayerStyle: FillLayer = {
  id: 'parcels-fill',
  type: 'fill',
  paint: {
    'fill-color': '#3b82f6',
    'fill-opacity': 0.2
  }
};

const lineLayerStyle: LineLayer = {
  id: 'parcels-line',
  type: 'line',
  paint: {
    'line-color': '#1d4ed8',
    'line-width': 2
  }
};

export default function MapViewer() {
  const mapRef = useRef<MapRef>(null);
  const [bbox, setBbox] = useState<number[] | null>(null);
  const [selectedUlpin, setSelectedUlpin] = useState<string | null>(null);

  // 1. Update Bounding Box when user finishes panning/zooming
  const onMoveEnd = useCallback(() => {
    if (mapRef.current) {
      const bounds = mapRef.current.getMap().getBounds();
      setBbox([
        bounds.getWest(),
        bounds.getSouth(),
        bounds.getEast(),
        bounds.getNorth()
      ]);
    }
  }, []);

  // 2. Fetch Parcels within BBOX
  const { data: parcels, isLoading: isLoadingParcels } = useQuery({
    queryKey: ['parcels', bbox],
    queryFn: async () => {
      if (!bbox) return [];
      const [minLon, minLat, maxLon, maxLat] = bbox;
      const res = await api.get(`/parcels?minLon=${minLon}&minLat=${minLat}&maxLon=${maxLon}&maxLat=${maxLat}`);
      return res.data;
    },
    enabled: !!bbox,
    staleTime: 60000, // Cache for 1 minute
  });

  // 3. Fetch Federated ROR Data when a parcel is clicked
  const { data: rorData, isLoading: isLoadingRor } = useQuery({
    queryKey: ['ror', selectedUlpin],
    queryFn: async () => {
      if (!selectedUlpin) return null;
      const res = await api.get(`/parcels/${selectedUlpin}/ror`);
      return res.data;
    },
    enabled: !!selectedUlpin
  });

  // Convert backend Parcel objects (with raw JTS geometry) to standard GeoJSON FeatureCollection
  const geojson = useMemo(() => {
    if (!parcels) return { type: 'FeatureCollection', features: [] };
    
    // Convert to strict GeoJSON
    const features = parcels.map((p: any) => ({
      type: 'Feature',
      properties: { ulpin: p.ulpin, localParcelId: p.localParcelId },
      geometry: p.geom 
    }));

    return { type: 'FeatureCollection', features };
  }, [parcels]);

  const onMapClick = (e: any) => {
    const features = e.features;
    if (features && features.length > 0) {
      const clickedUlpin = features[0].properties.ulpin;
      setSelectedUlpin(clickedUlpin);
    } else {
      setSelectedUlpin(null);
    }
  };

  return (
    <div className="relative w-full h-screen">
      <Map
        ref={mapRef}
        initialViewState={{
          longitude: 76.2675,
          latitude: 9.9313,
          zoom: 16
        }}
        mapStyle="https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
        interactiveLayerIds={['parcels-fill']}
        onClick={onMapClick}
        onMoveEnd={onMoveEnd}
        onLoad={onMoveEnd} // Get initial bounding box on load
      >
        <Source type="geojson" data={geojson as any}>
          <Layer {...fillLayerStyle} />
          <Layer {...lineLayerStyle} />
        </Source>
      </Map>

      {/* ROR Data Sidebar Panel */}
      {selectedUlpin && (
        <div className="absolute top-4 right-4 w-96 bg-white shadow-xl rounded-lg p-6 max-h-[90vh] overflow-y-auto">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-gray-800">Record of Rights</h2>
            <button onClick={() => setSelectedUlpin(null)} className="text-gray-500 hover:text-red-500">
              X
            </button>
          </div>
          
          <div className="mb-4 pb-4 border-b">
            <span className="text-xs text-gray-500 uppercase font-bold tracking-wider">ULPIN</span>
            <p className="font-mono text-sm mt-1">{selectedUlpin}</p>
          </div>

          {isLoadingRor ? (
            <div className="flex justify-center p-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : rorData ? (
            <div className="space-y-4">
              <div className="bg-green-50 text-green-700 p-3 rounded text-sm mb-4 border border-green-200">
                Data federated securely from State Node
              </div>
              
              {/* Dynamic rendering of canonical JSON */}
              {Object.entries(rorData).map(([key, value]) => (
                <div key={key}>
                  <span className="text-xs text-gray-500 uppercase font-bold tracking-wider">{key.replace(/_/g, ' ')}</span>
                  <p className="font-medium mt-1">{String(value)}</p>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-red-500">Failed to fetch federated governance data.</div>
          )}
        </div>
      )}

      {/* Loading Indicator for Map Pan */}
      {isLoadingParcels && (
        <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2 bg-blue-600 text-white px-4 py-2 rounded-full shadow flex items-center space-x-2">
          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
          <span className="text-sm font-medium">Fetching Parcels...</span>
        </div>
      )}
    </div>
  );
}
