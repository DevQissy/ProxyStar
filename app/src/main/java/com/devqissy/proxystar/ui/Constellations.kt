package com.devqissy.proxystar.ui

data class ConstellationShape(
    val id: String,
    val nameLatin: String,
    val points: List<Pair<Float, Float>>,
    val edges: List<Pair<Int, Int>>
)
object ConstellationCatalog {

    val ALL: List<ConstellationShape> = listOf(

        ConstellationShape(
            "uma", "Ursa Maior",
            points = listOf(
                -11.874f to -5.485f, -0.728f to -10.203f, -0.434f to -4.835f, -8.517f to -2.147f,
                -17.876f to -4.412f, -22.524f to -3.378f, -26.195f to 2.234f, -7.308f to 3.768f,
                -3.021f to 18.453f, -2.976f to 20.017f, -1.651f to 7.049f, 5.708f to 10.048f,
                6.522f to 8.633f, 13.606f to -11.514f, 23.131f to -9.171f, 10.581f to -7.491f,
                10.407f to -2.517f, 13.4f to -0.13f, 18.631f to 3.506f, 17.944f to 4.391f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 0, 0 to 4, 4 to 5, 5 to 6, 3 to 7,
                7 to 8, 8 to 9, 7 to 10, 10 to 11, 10 to 12, 1 to 13, 13 to 14, 14 to 15,
                15 to 2, 2 to 16, 16 to 17, 17 to 18, 19 to 17
            )
        ),
        ConstellationShape(
            "umi", "Ursa Minor",
            points = listOf(
                1.508f to 1.608f, -0.03f to 3.647f, 2.581f to 7.569f, 3.961f to 5.247f,
                -1.338f to -2.635f, -3.465f to -7.184f, -28.273f to -9.861f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 0, 0 to 4, 4 to 5, 5 to 6
            )
        ),
        ConstellationShape(
            "cas", "Cassiopeia",
            points = listOf(
                -6.623f to -3.608f, -3.057f to -0.173f, 0.574f to -0.655f, 2.596f to 3.525f,
                6.505f to 0.912f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4
            )
        ),
        ConstellationShape(
            "ori", "Orion",
            points = listOf(
                -10.138f to -8.305f, -6.862f to -13.813f, -9.231f to -13.675f, -11.223f to -7.746f,
                -8.849f to -3.184f, -7.058f to -0.944f, 0.404f to 0.114f, 7.915f to -3.688f,
                7.008f to 4.749f, 8.075f to 4.023f, 8.832f to 0.858f, 9.171f to -0.498f,
                8.979f to -2.437f, 7.549f to -7.051f, 5.512f to -8.941f, 4.238f to -9.134f,
                3.036f to 14.665f, 0.567f to 8.86f, -1.304f to 6.762f, -2.081f to -3.471f,
                -3.478f to 8.406f, -5.216f to 16.133f, -2.349f to 7.665f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 8 to 9,
                9 to 10, 10 to 11, 11 to 12, 12 to 7, 7 to 13, 13 to 14, 14 to 15, 16 to 17,
                17 to 18, 18 to 6, 6 to 19, 19 to 5, 5 to 20, 20 to 21, 20 to 22, 22 to 18
            )
        ),
        ConstellationShape(
            "sco", "Scorpius",
            points = listOf(
                10.364f to -6.459f, 10.052f to -9.952f, 8.977f to -12.768f, 5.658f to -6.981f,
                3.927f to -6.142f, 2.563f to -4.358f, -0.446f to 1.72f, -0.806f to 5.474f,
                -1.377f to 9.788f, -5.079f to 10.666f, -10.38f to 10.424f, -12.543f to 7.553f,
                -11.469f to 6.456f, -9.599f to 4.53f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 1 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8,
                8 to 9, 9 to 10, 10 to 11, 11 to 12, 12 to 13
            )
        ),
        ConstellationShape(
            "lyr", "Lyra",
            points = listOf(
                0.525f to -0.835f, 0.604f to -2.842f, 2.094f to -2.013f, -1.424f to -0.128f,
                -2.313f to 4.081f, -0.538f to 3.408f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 0, 0 to 3, 3 to 4, 4 to 5, 5 to 0
            )
        ),
        ConstellationShape(
            "cyg", "Cygnus",
            points = listOf(
                -12.309f to 10.099f, -7.216f to 6.356f, -2.645f to 0.07f, 4.455f to -4.804f,
                7.366f to -11.403f, 9.768f to -13.042f, -6.305f to -4.954f, 2.296f to 5.243f,
                7.172f to 12.367f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 6 to 2, 2 to 7, 7 to 8
            )
        ),
        ConstellationShape(
            "leo", "Leo",
            points = listOf(
                4.802f to 6.588f, 5.049f to 1.792f, 2.053f to -1.287f, -10.777f to -1.969f,
                -19.061f to 3.983f, -10.809f to 3.125f, 2.831f to -4.862f, 8.502f to -7.452f,
                10.14f to -5.219f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 0, 2 to 6, 6 to 7,
                7 to 8
            )
        ),
        ConstellationShape(
            "gem", "Gemini",
            points = listOf(
                11.357f to 0.384f, 9.495f to 0.377f, 4.665f to -2.241f, -1.601f to -7.355f,
                -7.003f to -8.998f, -9.472f to -5.136f, -7.309f to -4.005f, -3.67f to 0.908f,
                0.018f to 2.32f, 6.098f to 6.491f, 4.353f to 9.995f, -3.202f to 6.35f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8,
                8 to 9, 9 to 10, 7 to 11
            )
        ),
        ConstellationShape(
            "cen", "Centaurus",
            points = listOf(
                19.685f to 5.803f, 11.87f to 2.035f, 8.622f to 1.543f, 6.397f to 0.272f,
                -3.236f to 4.779f, -5.819f to -1.399f, -4.842f to -6.214f, -4.823f to -7f,
                -7.658f to -12.318f, -12.415f to -6.53f, -16.319f to -6.584f, -0.052f to -11.976f,
                -13.088f to 12.149f, -7.186f to 11.685f, 11.326f to 3.681f, 17.909f to 10.754f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8,
                8 to 9, 9 to 10, 7 to 11, 12 to 4, 4 to 13, 2 to 14, 14 to 15
            )
        ),
        ConstellationShape(
            "sge", "Sagitta",
            points = listOf(
                1.622f to 0.396f, -0.107f to -0.124f, -2.804f to -1.082f, 1.396f to 0.934f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 3 to 1
            )
        ),
        ConstellationShape(
            "tri", "Triangulum",
            points = listOf(
                2.157f to 2.419f, -1.333f to -2.989f, -2.981f to -1.849f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 0
            )
        ),
        ConstellationShape(
            "cma", "Canis Maior",
            points = listOf(
                7.46f to -3.906f, 2.251f to -5.145f, -1.896f to 1.972f, -3.142f to 4.532f,
                -1.593f to 6.073f, -0.876f to 7.111f, 8.014f to 8.202f, -6.785f to 7.442f,
                -0.298f to -4.807f, -2.067f to -6.228f, 0.154f to -9.823f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 7 to 3, 1 to 8,
                8 to 9, 9 to 10, 10 to 8
            )
        ),
        ConstellationShape(
            "aql", "Aquila",
            points = listOf(
                -1.816f to -5.598f, -2.943f to -3.853f, -4.071f to -1.392f, -8.053f to 5.837f,
                -3.363f to 4.009f, 3.354f to 1.9f, 8.357f to -8.848f, 8.148f to 9.898f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 1, 1 to 5,
                5 to 7
            )
        ),
        ConstellationShape(
            "tau", "Taurus",
            points = listOf(
                -19.901f to -7.037f, -4.935f to -2.403f, -3.175f to -1.765f, -1.025f to -1.522f,
                -1.786f to -3.437f, -3.164f to -5.074f, -17.148f to -14.502f, 3.61f to 1.616f,
                11.735f to 4.373f, 3.009f to 8.117f, 12.306f to 5.077f, 9.382f to 13.704f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 3 to 7, 7 to 8,
                8 to 9, 8 to 10, 10 to 11
            )
        ),
        ConstellationShape(
            "peg", "Pegasus",
            points = listOf(
                8.483f to -12.398f, 0.767f to -9.441f, -4.089f to -7.303f, -19.191f to -8.31f,
                -20.324f to 5.597f, -4.319f to 5.575f, -0.096f to 8.607f, 1.127f to 9.949f,
                8.434f to 14.582f, 14.514f to 10.905f, -0.87f to -3.821f, -0.058f to -2.785f,
                9.179f to -4.565f, 14.407f to -4.865f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8,
                8 to 9, 5 to 2, 2 to 10, 10 to 11, 11 to 12, 12 to 13
            )
        ),
        ConstellationShape(
            "crb", "Corona Borealis",
            points = listOf(
                2.405f to -3.32f, 3.531f to -1.067f, 2.017f to 1.324f, 0.24f to 1.743f,
                -1.272f to 1.971f, -3.036f to 1.161f, -3.886f to -1.812f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6
            )
        ),
        ConstellationShape(
            "crv", "Corvus",
            points = listOf(
                2.261f to 3.492f, 1.863f to 1.383f, 0.539f to -3.695f, -2.737f to -4.722f,
                -3.791f to 2.16f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 1
            )
        ),
        ConstellationShape(
            "sgr", "Sagittarius",
            points = listOf(
                9.246f to 9.122f, 7.796f to 6.745f, 8.5f to 2.189f, 6.955f to -2.218f,
                10.101f to -6.58f, -5.152f to 16.82f, -5.429f to 12.977f, -0.717f to 2.241f,
                3.038f to -0.648f, -12.378f to 14.229f, -13.369f to 7.637f, -12.506f to -1.34f,
                -8.268f to -2.756f, -5.736f to -3.131f, -3.58f to -2.383f, 0.91f to -1.343f,
                11.863f to 2.785f, -1.676f to 0.031f, -1.176f to -5.898f, -2.301f to -6.616f,
                -4.044f to -8.686f, -4.939f to -9.792f, -4.951f to -11.684f, 0.364f to -6.533f,
                1.153f to -4.894f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 5 to 6, 6 to 7, 7 to 8, 8 to 3,
                9 to 10, 10 to 11, 11 to 12, 12 to 13, 13 to 14, 14 to 15, 15 to 8, 8 to 2,
                2 to 16, 16 to 1, 1 to 7, 7 to 17, 17 to 15, 15 to 18, 18 to 19, 19 to 20,
                20 to 21, 21 to 22, 18 to 23, 23 to 24, 24 to 15
            )
        ),
        ConstellationShape(
            "del", "Delphinus",
            points = listOf(
                1.564f to 3.297f, 0.514f to 0.006f, 0.009f to -1.311f, -1.687f to -1.523f,
                -0.915f to -0.474f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 1
            )
        ),
        ConstellationShape(
            "and", "Andromeda",
            points = listOf(
                -18.489f to -4.189f, -7.838f to 2.52f, -1.86f to 7.28f, 4.223f to 9.05f,
                -5.376f to 14.723f, -3.435f to 13.874f, -1.708f to 8.829f, -1.379f to 4.421f,
                10.171f to -5.127f, 17.292f to -4.185f, 9.725f to -6.193f, 10.284f to -8.317f,
                -5.287f to -0.359f, -3.922f to -2.938f, -7.793f to -9.101f, -13.395f to -10.487f,
                8.619f to -8.28f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 4 to 5, 5 to 6, 6 to 2, 2 to 7, 7 to 8,
                8 to 9, 8 to 10, 10 to 11, 1 to 12, 12 to 13, 13 to 14, 14 to 15, 10 to 16
            )
        ),
        ConstellationShape(
            "cet", "Cetus",
            points = listOf(
                -9.513f to -5.786f, -7.658f to -8.143f, -5.731f to -11.01f, -9.923f to -12.664f,
                -13.613f to -11.457f, -14.253f to -6.639f, -8.559f to -2.878f, -3.53f to 0.428f,
                3.434f to 7.785f, 5.281f to 13.388f, 20.385f to 15.437f, 26.42f to 6.274f,
                14.141f to 7.633f, 10.287f to 5.634f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 0, 0 to 6, 6 to 7,
                7 to 8, 8 to 9, 9 to 10, 10 to 11, 11 to 12, 12 to 13, 13 to 8
            )
        ),
        ConstellationShape(
            "cmi", "Canis Minor",
            points = listOf(
                -1.508f to 1.532f, 1.508f to -1.532f
            ),
            edges = listOf(
                0 to 1
            )
        ),
        ConstellationShape(
            "lep", "Lepus",
            points = listOf(
                -8.029f to -2.293f, -5.701f to -3.06f, -3.445f to -2.406f, -0.048f to 0.594f,
                4.68f to -1.022f, 6.463f to 5.143f, 1.023f to 3.532f, -2.85f to 5.221f,
                -4.487f to 3.651f, 4.608f to -4.287f, 3.093f to -4.051f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8,
                9 to 4, 4 to 10
            )
        ),
        ConstellationShape(
            "lib", "Libra",
            points = listOf(
                3.453f to 6.002f, 6.566f to -3.238f, 0.401f to -9.897f, -3.97f to -4.491f,
                -4.323f to 8.855f, -4.708f to 10.498f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 1 to 3
            )
        ),
        ConstellationShape(
            "boo", "Bootes",
            points = listOf(
                8.34f to 15.312f, 6.78f to 14.371f, 2.371f to 13.586f, -1.028f to 2.397f,
                -1.081f to -5.54f, -7.359f to -7.622f, -10.209f to -0.546f, -3.794f to 5.695f,
                -2.987f to 19.04f, 2.219f to -13.32f, 2.833f to -19.019f, 0.366f to -19.082f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 2,
                2 to 8, 4 to 9, 9 to 10, 10 to 11, 11 to 9
            )
        ),
        ConstellationShape(
            "her", "Hercules",
            points = listOf(
                7.621f to 13.308f, 5.87f to 10.972f, 3.536f to 0.859f, 3.196f to -6.461f,
                5.051f to -9.976f, 8.08f to -13.852f, 10.395f to -12.474f, 13.79f to -9.99f,
                -0.473f to 1.535f, -3.586f to -4.348f, -12.278f to -4.789f, -5.407f to -4.685f,
                -3.583f to 7.622f, -10.212f to 4.741f, -12.597f to 3.213f, -14.659f to 3.699f,
                -3.501f to 18.071f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 2 to 8,
                3 to 9, 10 to 11, 11 to 9, 9 to 8, 8 to 12, 12 to 13, 13 to 14, 14 to 15,
                16 to 1
            )
        ),
        ConstellationShape(
            "dra", "Draco",
            points = listOf(
                -5.302f to 6.57f, -5.646f to 11.954f, -2.72f to 11.141f, -2.925f to 8.269f,
                -14.135f to -4.219f, -8.345f to -7.895f, -0.301f to -2.272f, 4.706f to 1.928f,
                7.176f to 4.877f, 11.308f to 4.476f, 20.31f to -0.933f, 30.471f to -6.346f,
                37.41f to -5.889f, -8.379f to -9.29f, -18.116f to -6.825f
            ),
            edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 0, 0 to 4, 4 to 5, 5 to 6, 6 to 7,
                7 to 8, 8 to 9, 9 to 10, 10 to 11, 11 to 12, 5 to 13, 4 to 14
            )
        )
    )
}
