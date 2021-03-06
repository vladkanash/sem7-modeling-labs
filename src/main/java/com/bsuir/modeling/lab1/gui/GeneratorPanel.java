package com.bsuir.modeling.lab1.gui;

import com.bsuir.modeling.lab1.constants.GUIConstants;
import com.bsuir.modeling.lab1.generator.RandomGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Vlad Kanash on 12.9.16.
 */
public class GeneratorPanel extends JPanel {

    private final JLabel expectedValue = new JLabel();
    private final JLabel variance = new JLabel();
    private final JLabel period = new JLabel();
    private final JLabel check = new JLabel();
    private final JLabel aperiod = new JLabel();
    private final JLabel standardDeviation = new JLabel();

    private final JPanel chartBlock = new JPanel();
    private final JPanel infoBlock = new JPanel();
    private final JPanel inputBlock = new JPanel();
    private final JPanel bottomBlock = new JPanel();

    public GeneratorPanel(RandomGenerator generator, boolean showStats) {
        if (showStats) {
            initInfoBlock();
        }
        initInputBlock(generator);
        initBottomBlock();
        initChartBlock(generator);

        this.add(chartBlock);
        this.add(bottomBlock);
        this.setBackground(Color.WHITE);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    private void initBottomBlock() {
        bottomBlock.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomBlock.setBackground(Color.WHITE);
        bottomBlock.add(inputBlock, BorderLayout.WEST);
        bottomBlock.add(infoBlock, BorderLayout.WEST);
    }

    private void initChartBlock(RandomGenerator generator) {

        final JPanel chartPanel = getNewChartPanel(Collections.emptyMap(), generator.getClass());

        chartBlock.setLayout(new BorderLayout());
        chartBlock.add(chartPanel);
    }

    private void initInputBlock(RandomGenerator generator) {
        inputBlock.setLayout(new BoxLayout(inputBlock, BoxLayout.PAGE_AXIS));
        inputBlock.setBackground(Color.WHITE);
        inputBlock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inputPanel = null;

        for (Map.Entry<String, Double> entry : generator.getInitParams().entrySet()) {
            final JTextField input = initTextField(entry.getValue());
            inputPanel = initInputPanel(input, entry.getKey());
            inputBlock.add(inputPanel);
        }

        setInputVerifiers();

        if (inputPanel != null) {
            inputPanel.add(new JButton(new AbstractAction(GUIConstants.BUTTON_LABEL) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateData(generator);
                }
            }));
        }
    }

    private void updateData(RandomGenerator generator) {

        Set<String> paramNames = generator.getInitParams().keySet();
        Map<String, Double> newParams = new HashMap<>();

        for (Component inputPanel : inputBlock.getComponents()) {
            String name = inputPanel.getName();
            if (inputPanel instanceof JPanel && paramNames.contains(name)) {
                for (Component textField : ((JPanel) inputPanel).getComponents()) {
                    if (textField instanceof JTextField ) {
                        newParams.put(name, Double.valueOf(((JTextField) textField).getText()));
                    }
                }
            }
        }

        chartBlock.removeAll();
        chartBlock.revalidate();
        chartBlock.setLayout(new BorderLayout());
        chartBlock.add(getNewChartPanel(newParams, generator.getClass()));
        chartBlock.repaint();
    }

    private JPanel initInputPanel(JTextField inputR, String text) {
        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(inputR);
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(GUIConstants.ENTER_LABEL_TEXT + text));
        panel.setName(text);
        return panel;
    }

    private void initInfoBlock() {
        infoBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.PAGE_AXIS));
        infoBlock.setBackground(Color.WHITE);
        infoBlock.add(expectedValue);
        infoBlock.add(variance);
        infoBlock.add(standardDeviation);
        infoBlock.add(check);
        infoBlock.add(period);
        infoBlock.add(aperiod);
    }

    private void setInputVerifiers() {
        final InputVerifier verifier = new NotEmptyInputVerifier();
        expectedValue.setInputVerifier(verifier);
        variance.setInputVerifier(verifier);
        standardDeviation.setInputVerifier(verifier);
        check.setInputVerifier(verifier);
        period.setInputVerifier(verifier);
        aperiod.setInputVerifier(verifier);
    }

    private JTextField initTextField(double value) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setGroupingUsed(false);
        final JTextField input = new JFormattedTextField(decimalFormat);
        input.setText(String.valueOf(value));
        input.setColumns(10);
        return input;
    }

    private JPanel getNewChartPanel(Map<String, Double> params,
                                           Class<? extends RandomGenerator> generatorClass) {
        RandomGenerator generator = getGeneratorInstance(params, generatorClass);
        final double[] values = generator.getStream().limit(GUIConstants.RANDOM_LIMIT).toArray();
        updateLabels(values, generator);
        return ChartService.generateFrequencyHistogramPanel(values);
    }

    private RandomGenerator getGeneratorInstance(Map<String, Double> params,
                                                        Class<? extends RandomGenerator> generatorClass) {
        RandomGenerator generator = null;
        try {
            Constructor<? extends RandomGenerator> cons = generatorClass.getConstructor(Map.class);
            generator = cons.newInstance(params);
            } catch (InstantiationException | IllegalAccessException |
                NoSuchMethodException | InvocationTargetException e) {

                System.out.println("Unable to create instance of " + generatorClass);
                e.printStackTrace();
            }
        return generator;
    }

    private void updateLabels(double[] values, RandomGenerator generator) {
        expectedValue.setText(LabelUtils.getExpectedValueLabel(values));
        variance.setText(LabelUtils.getVarianceString(values));
        check.setText(LabelUtils.getCheckString(values));
        aperiod.setText(LabelUtils.getAPeriodString(generator));
        period.setText(LabelUtils.getPeriodString(generator));
        standardDeviation.setText(LabelUtils.getSkoString(values));
    }
}